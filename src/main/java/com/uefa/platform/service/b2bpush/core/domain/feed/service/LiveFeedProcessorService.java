package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.PlatformMessage.Type;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageMetadataProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.converter.B2bStaticFeedDataConverter;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util.ParameterValueResolver;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LiveFeedProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveFeedProcessorService.class);

    private final FeedRepository feedRepository;

    private final ClientRepository clientRepository;

    private final FeedMessageProducer feedMessageProducer;

    private final ObjectMapper objectMapper;

    private final ParameterValueResolver parameterValueResolver;

    private final LiveEventHandler liveEventHandler;

    private final FeedMessageMetadataProducer feedMessageMetadataProducer;

    @Autowired
    public LiveFeedProcessorService(FeedRepository feedRepository, ClientRepository clientRepository,
                                    FeedMessageProducer feedMessageProducer, ObjectMapper objectMapper,
                                    ParameterValueResolver parameterValueResolver, LiveEventHandler liveEventHandler,
                                    FeedMessageMetadataProducer feedMessageMetadataProducer) {
        this.feedRepository = feedRepository;
        this.clientRepository = clientRepository;
        this.feedMessageProducer = feedMessageProducer;
        this.objectMapper = objectMapper;
        this.parameterValueResolver = parameterValueResolver;
        this.liveEventHandler = liveEventHandler;
        this.feedMessageMetadataProducer = feedMessageMetadataProducer;
    }


    public List<String> processMessage(Type type, Map<String, String> parameters, Object data, Object clientFeedConfigurationId) {
        List<String> clientsSent = new ArrayList<>();
        //get active Live feeds
        final Map<String, Feed> activeFeeds = feedRepository.findAll().stream()
                .filter(feed -> Status.ACTIVE.equals(feed.getStatus()))
                .filter(feed -> FeedType.LIVE.equals(feed.getType()))
                .collect(Collectors.toMap(Feed::getId, Function.identity()));
        LOGGER.info("Active feeds to process: {}", activeFeeds.size());

        //get active clients
        final List<Client> activeClients;
        if (clientFeedConfigurationId != null) {
            activeClients = clientRepository.findAllActiveClientsByConfigurationId(clientFeedConfigurationId.toString());
        } else {
            activeClients = clientRepository.findAll().stream()
                    .filter(client -> Status.ACTIVE.equals(client.getStatus()))
                    .toList();
        }
        LOGGER.info("Active clients to process: {}", activeClients.size());

        activeClients.forEach(client -> client.getConfigurations().stream()
                .filter(feedConfiguration -> Status.ACTIVE.equals(feedConfiguration.getStatus()))
                //get only configurations for active feeds
                .filter(feedConfiguration -> activeFeeds.get(feedConfiguration.getFeedId()) != null &&
                        activeFeeds.get(feedConfiguration.getFeedId()).getLiveDataType() != null)
                //get only feed with the same tpe as message received
                .filter(feedConfiguration -> type.toString().equals(activeFeeds.get(feedConfiguration.getFeedId()).getLiveDataType().toString()))
                .forEach(feedConfiguration -> {
                    final String clientName =
                            sendLiveMessage(feedConfiguration, parameters, activeFeeds, liveEventHandler.processMessage(data, client), client);
                    if (clientName != null) {
                        clientsSent.add(clientName);
                    }
                }));

        return clientsSent;
    }

    private String sendLiveMessage(FeedConfiguration feedConfiguration, Map<String, String> parameters,
                                   Map<String, Feed> activeFeeds, Object data, Client client) {

        //update last processing times for debugging purposes
        feedRepository.upsert(Feed.FeedUpdateBuilder.create().setLastProcessingTime(Instant.now()).build(), feedConfiguration.getFeedId());

        //convert and send message
        if (parametersMatch(feedConfiguration.getParameters(), parameters)) {
            try {
                var activeFeedCode = activeFeeds.get(feedConfiguration.getFeedId()).getCode();
                var metadata = feedMessageMetadataProducer.createMetadata(data, activeFeedCode, getFeedParameters(parameters), Type.B2B_LIVE_FEED);
                feedMessageProducer.send(B2bStaticFeedDataConverter.convertPushDto(
                                objectMapper.writeValueAsString(data),
                                activeFeedCode,
                                getFeedParameters(parameters)),
                        client.getRoutingKey(),
                        Type.B2B_LIVE_FEED,
                        metadata,
                        feedConfiguration.isPayloadSharedToClient());
            } catch (JsonProcessingException e) {
                LOGGER.error("Error while deserializing message value as string: , {}", data);
            }
            //update lastSent date
            clientRepository.updateLastSentDate(client.getId(), feedConfiguration.getId());
            return client.getName();
        }
        return null;
    }

    private boolean parametersMatch(List<Client.FeedConfiguration.Parameter> feedParameters,
                                    Map<String, String> messageParameters) {

        for (Client.FeedConfiguration.Parameter parameter : feedParameters) {
            List<String> parametersValues = parameterValueResolver.getParameterValues(parameter.getValue(), parameter.getName());
            if (!messageParameters.containsKey(parameter.getName()) ||
                    !parametersValues.contains(messageParameters.get(parameter.getName()))) {
                return false;
            }
        }
        return true;
    }

    private List<Client.FeedConfiguration.Parameter> getFeedParameters(Map<String, String> messageParameters) {
        List<Client.FeedConfiguration.Parameter> result = new ArrayList<>();
        messageParameters.forEach((k, v) -> result.add(new Client.FeedConfiguration.Parameter(k, v, null, null)));
        return result;
    }
}
