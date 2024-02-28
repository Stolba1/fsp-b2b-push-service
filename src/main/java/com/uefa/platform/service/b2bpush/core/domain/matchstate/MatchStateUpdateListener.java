package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MatchStateUpdateListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MatchStateUpdateListener.class);

    private final MatchStateUpdateHandler matchStateUpdateHandler;
    private final ObjectMapper platformMessageMapper;

    public MatchStateUpdateListener(MatchStateUpdateHandler matchStateUpdateHandler,
                                    ObjectMapper platformMessageMapper) {
        this.matchStateUpdateHandler = matchStateUpdateHandler;
        this.platformMessageMapper = platformMessageMapper;
    }

    @RabbitListener(queues = "${rabbit.update-queue-name}")
    @Override
    public void onMessage(Message message) {
        try {
            PlatformMessage plaformMessage = platformMessageMapper.readValue(message.getBody(), PlatformMessage.class);
            PlatformMessage.Type type = plaformMessage.getType();

            if (PlatformMessage.Type.MATCH_STATE.equals(type)) {
                matchStateUpdateHandler.handleUpdate((MatchStateMessage) plaformMessage);
            } else {
                LOG.warn("Received unsupported message type {}", type);
            }
        } catch (Exception e) {
            LOG.error("Error handling message", e);
        }
    }

}
