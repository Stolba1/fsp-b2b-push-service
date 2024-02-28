package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.uefa.platform.dto.message.B2bMatchStateMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyData;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchStateMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private MatchStateMessageProducer producer;

    @Test
    void testSend() {
        B2bMatchStateData data = new B2bMatchStateData(dummyData(), null, null, null);
        B2bMatchStateMessage b2bMatchStateMessage = new B2bMatchStateMessage(data);

        ArgumentCaptor<MessagePostProcessor> messagePostProcessorArgumentCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        when(message.getMessageProperties()).thenReturn(messageProperties);

        producer.send(b2bMatchStateMessage);

        verify(rabbitTemplate).convertAndSend(any(), eq(""), eq(b2bMatchStateMessage), messagePostProcessorArgumentCaptor.capture());

        MessagePostProcessor messagePostProcessor = messagePostProcessorArgumentCaptor.getValue();
        messagePostProcessor.postProcessMessage(message);

        verify(message).getMessageProperties();
        verify(messageProperties).setHeader("Version", 1);
        verify(messageProperties).setType("LIVE.MATCH_STATE");
    }
}
