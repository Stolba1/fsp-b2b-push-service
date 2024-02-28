package com.uefa.platform.service.b2bpush.core.domain.feed.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.LiveFeedHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RabbitLiveFeedUpdateListener implements MessageListener {

    private static final Set<PlatformMessage.Type> ALLOWED_TYPES = Set.of(
            PlatformMessage.Type.MATCH_PLAYER_STATISTICS,
            PlatformMessage.Type.MATCH_TEAM_STATISTICS,
            PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
            PlatformMessage.Type.COMPETITION_TEAM_STATISTICS,
            PlatformMessage.Type.MATCH_STATE,
            PlatformMessage.Type.TRANSLATIONS,
            PlatformMessage.Type.PRE_MATCH);


    private static final Logger LOG = LoggerFactory.getLogger(RabbitLiveFeedUpdateListener.class);

    private final LiveFeedHandlerService liveFeedProcessorService;
    private final ObjectMapper platformMessageMapper;

    public RabbitLiveFeedUpdateListener(LiveFeedHandlerService liveFeedProcessorService,
                                        ObjectMapper platformMessageMapper) {
        this.liveFeedProcessorService = liveFeedProcessorService;
        this.platformMessageMapper = platformMessageMapper;
    }

    @RabbitListener(queues = "${rabbit.live-feeds-queue-name}")
    @Override
    public void onMessage(Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object clientFeedConfigurationId = headers.get("clientFeedConfigurationId");

        try {
            PlatformMessage plaformMessage = platformMessageMapper.readValue(message.getBody(), PlatformMessage.class);
            PlatformMessage.Type type = plaformMessage.getType();

            if (ALLOWED_TYPES.contains(type)) {
                liveFeedProcessorService.handleMessage(plaformMessage, clientFeedConfigurationId);
            } else {
                LOG.warn("Received unsupported message type {}", type);
            }
        } catch (Exception e) {
            LOG.error("Error handling message", e);
        }
    }

}
