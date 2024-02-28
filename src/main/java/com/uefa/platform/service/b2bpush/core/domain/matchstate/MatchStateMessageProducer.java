package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.uefa.platform.dto.message.B2bMatchStateMessage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.RabbitContentType;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.RabbitMessageTypeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MatchStateMessageProducer {

    @Value("${rabbit.state-exchange-name}")
    private String exchange;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MatchStateMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(B2bMatchStateMessage message) {
        rabbitTemplate.convertAndSend(exchange, "", message, m -> {
            var properties = m.getMessageProperties();
            properties.setType(RabbitMessageTypeUtil.formatMessageType(RabbitContentType.LIVE, "MATCH_STATE"));
            properties.setHeader("Version", 1);
            return m;
        });
    }

}
