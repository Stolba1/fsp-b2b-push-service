package com.uefa.platform.service.b2bpush.core.domain.feed;

import com.uefa.platform.dto.message.B2bFeedMessage;
import com.uefa.platform.dto.message.CommandMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bFeedData;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.RabbitContentType;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.RabbitMessageTypeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeedMessageProducer {

    @Value("${rabbit.feed-exchange-name}")
    private String exchange;

    @Value("${rabbit.commands-exchange-name}")
    private String commandExchange;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public FeedMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(B2bFeedData data, String routingKey, PlatformMessage.Type type, Map<String, String> metadata, boolean includePayload) {
        final var messagePayload = includePayload ? new B2bFeedMessage(data, type) : "";
        rabbitTemplate.convertAndSend(exchange, routingKey, messagePayload, message -> {
            var properties = message.getMessageProperties();
            properties.setType(RabbitMessageTypeUtil.formatMessageType(
                    PlatformMessage.Type.B2B_LIVE_FEED.equals(type) ? RabbitContentType.LIVE : RabbitContentType.STATIC,
                    data.getInfo().getFeedName())
            );
            properties.setHeader("Version", 2);
            metadata.forEach(properties::setHeader);

            return message;
        });
    }

    public void sendCommand(CommandMessage commandMessage, String routingKey) {
        rabbitTemplate.convertAndSend(commandExchange, routingKey, commandMessage);
    }
}
