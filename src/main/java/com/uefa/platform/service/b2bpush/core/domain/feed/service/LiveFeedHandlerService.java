package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.LiveFeedMessage;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.converter.MessageProviderConverter;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.matchstate.ArchiveTags.CLIENTS_SENT;

@Service
public class LiveFeedHandlerService {

    public static final int DEFAULT_RETENTION_DAYS = 30;
    private static final String COMPETITION_ID_PARAMETER = "competitionId";
    private static final String SEASON_YEAR_PARAMETER = "seasonYear";
    private static final String MATCH_ID_PARAMETER = "matchId";

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveFeedHandlerService.class);

    private final MessageArchiveBuilder messageArchiveBuilder;

    private final ObjectMapper objectMapper;

    private final LiveFeedProcessorService liveFeedProcessorService;

    private final FeedRepository feedRepository;

    @Autowired
    public LiveFeedHandlerService(MessageArchiveBuilder messageArchiveBuilder, ObjectMapper objectMapper, LiveFeedProcessorService liveFeedProcessorService,
                                  FeedRepository feedRepository) {
        this.messageArchiveBuilder = messageArchiveBuilder;
        this.objectMapper = objectMapper;
        this.liveFeedProcessorService = liveFeedProcessorService;
        this.feedRepository = feedRepository;
    }

    public void handleMessage(PlatformMessage message, Object clientFeedConfigurationId) {
        final Instant receivedAt = Instant.now();
        MessageArchive archive = null;
        try {
            final MessageArchiveEntity.MessageProvider provider = MessageProviderConverter.convertMessageProvider(message.getProvider());

            List<String> clientsSent = new ArrayList<>();

            if (message instanceof LiveFeedMessage) {
                archive = messageArchiveBuilder.build(provider, null, receivedAt);
                clientsSent = handleLiveFeedUpdateMessage((LiveFeedMessage) message, archive, clientFeedConfigurationId);
            } else if (message instanceof MatchStateMessage) {
                archive = messageArchiveBuilder.build(provider, objectMapper.writeValueAsString(message), receivedAt);
                clientsSent = handleMatchStateMessage((MatchStateMessage) message, archive, clientFeedConfigurationId);
            } else {
                archive = messageArchiveBuilder.build(provider, objectMapper.writeValueAsString(message), receivedAt);
            }

            final Optional<Feed> feed = getFeedByMessageType(message.getType().toString());
            final int retentionDays = feed.isPresent() && feed.get().getRetentionDays() != null ? feed.get().getRetentionDays() : DEFAULT_RETENTION_DAYS;
            archive.withDeleteDate(receivedAt.plus(retentionDays, ChronoUnit.DAYS));
            archive.withFeedType(FeedType.LIVE);
            archive.withFeedName(feed.isPresent() ? feed.get().getCode() : "");
            archive.withStatus(MessageArchiveEntity.Status.SUCCESS)
                    .withTag(CLIENTS_SENT, clientsSent)
                    .withSentTimestamp(Instant.now())
                    //we add this debug tag as having both implementations in place we are going to store twice the messages in the raw repo
                    .withTag(ArchiveTags.SOURCE_QUEUE, "live-feeds-queue");

        } catch (Exception e) {
            if (archive != null) {
                archive.withException(e.getClass() + ":" + e.getMessage()).withStatus(MessageArchiveEntity.Status.ERROR);
                LOGGER.error(String.format("Error handling message with archiveId:%s", archive.getArchiveId()), e);
            }
        } finally {
            if (archive != null) {
                archive.save();
            }
        }

    }

    private List<String> handleMatchStateMessage(MatchStateMessage matchStateMessage, MessageArchive archive, Object clientFeedConfigurationId)
            throws JsonProcessingException {
        // comment the enrichment of the b2b push messages as part of the following story https://jira.uefa.com/browse/FSP-2068
        final B2bMatchStateData converted = new B2bMatchStateData(matchStateMessage.getData().copyWithoutProviderData(), null, null, null);
        //statically create parameters
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(COMPETITION_ID_PARAMETER, converted.getMatchStateData().getMatchInfo().getData().getCompetitionId());
        parameters.put(SEASON_YEAR_PARAMETER, converted.getMatchStateData().getMatchInfo().getData().getSeasonYear());
        parameters.put(MATCH_ID_PARAMETER, converted.getMatchStateData().getMatchInfo().getData().getId());

        List<String> clientsSent = liveFeedProcessorService.processMessage(matchStateMessage.getType(), parameters, converted, clientFeedConfigurationId);


        archive.withTag(ArchiveTags.MATCH_ID, converted.getMatchStateData().getMatchInfo().getData().getId())
                .withTag(ArchiveTags.HAS_LINEUP, converted.getMatchStateData().getLineupUpdate() != null)
                .withSentContent(objectMapper.writeValueAsString(converted));

        if (converted.getMatchStateData().getEventUpdates() != null) {
            final Set<String> eventIds =
                    converted.getMatchStateData().getEventUpdates().stream().map(MatchStateData.MatchEventWrapper::getEventId).collect(Collectors.toSet());
            archive.withTag(ArchiveTags.EVENT_IDS, eventIds);
        }
        if (converted.getMatchStateData().getOfficialsUpdate() != null) {
            archive.withTag(ArchiveTags.HAS_OFFICIALS, true);

        }
        return clientsSent;
    }

    private List<String> handleLiveFeedUpdateMessage(LiveFeedMessage liveFeedMessage, MessageArchive archive, Object clientFeedConfigurationId)
            throws JsonProcessingException {
        List<String> clientsSent = liveFeedProcessorService.processMessage(liveFeedMessage.getType(), liveFeedMessage.getData().getMetadata(),
                liveFeedMessage.getData().getData(), clientFeedConfigurationId);

        archive.withSentContent(objectMapper.writeValueAsString(liveFeedMessage.getData().getData()));

        for (Map.Entry<String, String> entry : liveFeedMessage.getData().getMetadata().entrySet()) {
            archive.withTag(entry.getKey(), entry.getValue());
        }

        return clientsSent;
    }

    private Optional<Feed> getFeedByMessageType(String messageType) {
        Optional<Feed> feed;
        try {
            feed = feedRepository.findActiveByLiveDataType(LiveFeedDataType.valueOf(messageType));
        } catch (IllegalArgumentException e) {
            feed = Optional.empty();
        }
        return feed;
    }
}
