package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.amazonaws.util.CollectionUtils;
import com.uefa.platform.client.competition.v2.SeasonClient;
import com.uefa.platform.dto.competition.v2.Season;
import com.uefa.platform.dto.message.PlatformMessage.Type;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageMetadataProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.converter.B2bStaticFeedDataConverter;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util.ParameterValueResolver;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedConfigurationEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Hash;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessStaticFeedResult;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessingSource;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.HashRepository;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.ArchiveTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.feed.service.LiveFeedHandlerService.DEFAULT_RETENTION_DAYS;

@Service
public class FeedProcessorService {

    private static final String COMPETITION_ID_PARAMETER = "competitionId";
    private static final String COMPETITION_IDS_PARAMETER = "competitionIds";
    private static final String SEASON_YEAR_PARAMETER = "seasonYear";
    private static final String CURRENT_PARAMETER = "current";

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedProcessorService.class);

    private final FeedRepository feedRepository;

    private final ClientRepository clientRepository;

    private final ConfigurationProcessorService configurationProcessorService;

    private final ParameterValueResolver parameterValueResolver;

    private final HashRepository hashRepository;

    private final SeasonClient seasonClient;

    private final FeedMessageProducer feedMessageProducer;

    private final FeedMessageMetadataProducer feedMessageMetadataProducer;
    private final MessageArchiveBuilder messageArchiveBuilder;

    @Autowired
    public FeedProcessorService(FeedRepository feedRepository, ClientRepository clientRepository,
                                ConfigurationProcessorService configurationProcessorService1,
                                ParameterValueResolver parameterValueResolver,
                                HashRepository hashRepository,
                                SeasonClient seasonClient,
                                FeedMessageProducer feedMessageProducer,
                                FeedMessageMetadataProducer feedMessageMetadataProducer, MessageArchiveBuilder messageArchiveBuilder) {
        this.feedRepository = feedRepository;
        this.clientRepository = clientRepository;
        this.configurationProcessorService = configurationProcessorService1;
        this.parameterValueResolver = parameterValueResolver;
        this.hashRepository = hashRepository;
        this.seasonClient = seasonClient;
        this.feedMessageProducer = feedMessageProducer;
        this.feedMessageMetadataProducer = feedMessageMetadataProducer;
        this.messageArchiveBuilder = messageArchiveBuilder;
    }

    public void processStaticFeeds() {
        //get active feeds which require refresh
        final List<Feed> activeFeeds = feedRepository.findAll().stream()
                .filter(feed -> Status.ACTIVE.equals(feed.getStatus()))
                //todo: this null check needs to be removed when we update all the feeds to have type
                .filter(feed -> feed.getType() == null || FeedType.STATIC.equals(feed.getType()))
                .filter(feed -> feed.getLastProcessingTime() == null ||
                        Instant.now().isAfter(feed.getLastProcessingTime().plus(feed.getProcessEveryMinutes(), ChronoUnit.MINUTES)))
                .toList();
        LOGGER.info("Active feeds to process: {}", activeFeeds.size());

        //get active clients
        final List<Client> activeClients = clientRepository.findAll().stream()
                .filter(client -> Status.ACTIVE.equals(client.getStatus())).toList();
        LOGGER.info("Active clients to process: {}", activeClients.size());
        //compute set of configuration and clients
        processStaticFeedsConfigurations(activeFeeds, activeClients, null, ProcessingSource.SCHEDULED);
    }

    public void processStaticFeedsConfigurations(List<Feed> activeFeeds, List<Client> activeClients, String configurationId,
                                                 ProcessingSource processingSource) {
        final Map<String, Feed> feedsMap = activeFeeds.stream().collect(Collectors.toMap(Feed::getId, Function.identity()));

        //update last processing times so no other thread can take them for further processing (locking mechanism)
        feedsMap.forEach((id, feed) ->
                feedRepository.upsert(Feed.FeedUpdateBuilder.create().setLastProcessingTime(Instant.now()).build(), id)
        );

        final Map<FeedConfigurationEntity, List<Client>> uniqueFeedConfigsWithClients =
                parameterValueResolver.calculateUniqueFeedsWithClients(activeClients, feedsMap);

        //get feed results per configuration
        for (FeedConfigurationEntity entity : uniqueFeedConfigsWithClients.keySet()) {
            final Instant sentAt = Instant.now();
            MessageArchive archive = buildMessageArchive(feedsMap.get(entity.getFeedId()), sentAt, entity);
            List<String> sentClients = new ArrayList<>();
            try {
                if (configurationId != null && !configurationId.equals(entity.getConfigurationId())) {
                    continue;
                }

                ProcessStaticFeedResult result = configurationProcessorService.processStaticFeed(entity);
                if (result == null) {
                    continue;
                } else {
                    archive.withTag(ArchiveTags.PARAMETERS, result.getParameters());
                    archive.withTag(ArchiveTags.FINAL_URL, result.getFinalUrl());
                }
                sentClients = processEntity(processingSource, uniqueFeedConfigsWithClients, entity, result);
            } catch (Exception e) {
                archive.withException(e.getClass() + ":" + e.getMessage()).withStatus(MessageArchiveEntity.Status.ERROR);
                LOGGER.error(String.format("Error sending static message with archiveId:%s", archive.getArchiveId()), e);
            } finally {
                if (!CollectionUtils.isNullOrEmpty(sentClients)) {
                    archive.withTag(ArchiveTags.CLIENTS_SENT, sentClients);
                    archive.save();
                }
            }
        }
    }

    private List<String> processEntity(ProcessingSource processingSource,
                                       Map<FeedConfigurationEntity, List<Client>> uniqueFeedConfigsWithClients, FeedConfigurationEntity entity,
                                       ProcessStaticFeedResult result) {
        List<String> sentClients = new ArrayList<>();
        final List<Client> clients = uniqueFeedConfigsWithClients.get(entity);
        for (Client client : clients) {
            //old Hash from repository
            final Optional<Hash> oldHash =
                    hashRepository.findById(new Hash.HashIdentifier(result.getFinalUrl(), entity.getConfigurationId(), client.getId()));
            //check if hash is new
            //if processing is done with the bootstrap we don't need to check if the hash is changed
            if (checkHash(result.getHash(), oldHash) || processingSource.equals(ProcessingSource.BOOTSTRAP)) {

                sentClients.add(client.getName());
                //check if the season year is current and modify the params to send in the header and in DTO
                List<Client.FeedConfiguration.Parameter> actualParameters = getActualSeasonYearParameters(entity.getParameters());
                //convert and send message
                Map<String, String> metadata = feedMessageMetadataProducer.createMetadata(result.getData(), entity.getFeedCode(), actualParameters,
                        Type.B2B_STATIC_FEED);
                feedMessageProducer.send(
                        B2bStaticFeedDataConverter.convertPushDto(result.getData(), entity.getFeedCode(), actualParameters),
                        client.getRoutingKey(),
                        Type.B2B_STATIC_FEED,
                        metadata,
                        entity.isSharePayloadWithClient());
                //update lastSent date
                clientRepository.updateLastSentDate(client.getId(), entity.getConfigurationId());
                //update hash
                hashRepository.save(
                        new Hash(new Hash.HashIdentifier(result.getFinalUrl(), entity.getConfigurationId(), client.getId()), result.getHash(),
                                Instant.now()));

            } else {
                //hash is the same, do nothing
                LOGGER.info("Hash for feed {} with url {} is the same, do not send it!", entity.getFeedCode(), result.getFinalUrl());
            }
        }
        return sentClients;
    }

    private MessageArchive buildMessageArchive(Feed feed, Instant sentAT, FeedConfigurationEntity entity) {
        MessageArchive archive = messageArchiveBuilder.buildStaticMessageArchive();
        Integer retentionDays = feed.getRetentionDays();
        archive.withSentTimestamp(sentAT);
        archive.withDeleteDate(sentAT.plus(retentionDays != null ? retentionDays : DEFAULT_RETENTION_DAYS, ChronoUnit.DAYS));
        archive.withStatus(MessageArchiveEntity.Status.SUCCESS);
        archive.withFeedName(entity.getFeedCode());
        archive.withFeedType(FeedType.STATIC);
        return archive;
    }


    private boolean checkHash(String hash, Optional<Hash> oldHashEntity) {
        return (hash != null && (oldHashEntity.isEmpty() || !hash.equals(oldHashEntity.get().getHash())));
    }

    private List<Client.FeedConfiguration.Parameter> getActualSeasonYearParameters(final Map<String, String> parameters) {
        Map<String, String> resultParameters = new HashMap<>(parameters);

        if (parameters.containsKey(SEASON_YEAR_PARAMETER) && CURRENT_PARAMETER.equals(parameters.get(SEASON_YEAR_PARAMETER)) &&
                (parameters.containsKey(COMPETITION_ID_PARAMETER) || parameters.containsKey(COMPETITION_IDS_PARAMETER))) {
            //try to get competition id/ids
            final String competitionId =
                    parameters.containsKey(COMPETITION_ID_PARAMETER) ? parameters.get(COMPETITION_ID_PARAMETER) : parameters.get(COMPETITION_IDS_PARAMETER);
            seasonClient.getCurrentSeason(competitionId)
                    .doOnError(throwable -> LOGGER.error("Could not determine current season year for competition: {}", competitionId, throwable))
                    .onErrorReturn(new Season("", CURRENT_PARAMETER, null, "", null, null, "", ""))
                    .doOnSuccess(season1 -> {
                        if (season1 != null) {
                            resultParameters.put(SEASON_YEAR_PARAMETER, season1.getYear());
                        }
                    })
                    .block();
        }
        return resultParameters.entrySet().stream()
                .map(param -> new Client.FeedConfiguration.Parameter(param.getKey(), param.getValue(), null, null))
                .collect(Collectors.toList());
    }

}
