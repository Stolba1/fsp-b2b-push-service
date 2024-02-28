package com.uefa.platform.service.b2bpush.core.domain.feed;

import com.uefa.platform.dto.message.B2bFeedMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bFeedData;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedMessageProducerTest {

    private final static ArgumentCaptor<MessagePostProcessor> messagePostProcessorArgumentCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private FeedMessageProducer producer;


    @BeforeEach
    void setUp() {
        when(message.getMessageProperties()).thenReturn(messageProperties);
    }

    @Test
    void testStaticSend() {
        B2bFeedData data = new B2bFeedData(new B2bFeedData.Info("TEST_FEED_1", Collections.emptyList()), "{}");
        B2bFeedMessage b2bFeedMessage = new B2bFeedMessage(data, PlatformMessage.Type.B2B_STATIC_FEED);

        producer.send(data, "TEST_KEY_1", PlatformMessage.Type.B2B_STATIC_FEED, Map.of("metadata1", "value1"), true);

        verify(rabbitTemplate).convertAndSend(any(), eq("TEST_KEY_1"), eq(b2bFeedMessage), messagePostProcessorArgumentCaptor.capture());

        MessagePostProcessor messagePostProcessor = messagePostProcessorArgumentCaptor.getValue();
        messagePostProcessor.postProcessMessage(message);

        verify(message).getMessageProperties();
        verify(messageProperties).setHeader("Version", 2);
        verify(messageProperties).setHeader("metadata1", "value1");
        verify(messageProperties).setType("STATIC.TEST_FEED_1");
    }

    @Test
    void testStaticSendNoPayload() {
        B2bFeedData data = new B2bFeedData(new B2bFeedData.Info("TEST_FEED_1", Collections.emptyList()), "{}");

        producer.send(data, "TEST_KEY_1", PlatformMessage.Type.B2B_STATIC_FEED, Map.of("metadata1", "value1"), false);

        verify(rabbitTemplate).convertAndSend(any(), eq("TEST_KEY_1"), eq(""), messagePostProcessorArgumentCaptor.capture());

        MessagePostProcessor messagePostProcessor = messagePostProcessorArgumentCaptor.getValue();
        messagePostProcessor.postProcessMessage(message);

        verify(message).getMessageProperties();
        verify(messageProperties).setHeader("Version", 2);
        verify(messageProperties).setHeader("metadata1", "value1");
        verify(messageProperties).setType("STATIC.TEST_FEED_1");
    }

    @Test
    void testSendLiveData() {
        B2bFeedData data = new B2bFeedData(new B2bFeedData.Info("TEST_FEED_2", Collections.emptyList()), "{}");
        B2bFeedMessage b2bFeedMessage = new B2bFeedMessage(data, PlatformMessage.Type.B2B_LIVE_FEED);

        producer.send(data, "TEST_KEY_2", PlatformMessage.Type.B2B_LIVE_FEED, Map.of("metadata1", "value1"), true);

        verify(rabbitTemplate).convertAndSend(any(), eq("TEST_KEY_2"), eq(b2bFeedMessage), messagePostProcessorArgumentCaptor.capture());

        MessagePostProcessor messagePostProcessor = messagePostProcessorArgumentCaptor.getValue();
        messagePostProcessor.postProcessMessage(message);

        verify(message).getMessageProperties();
        verify(messageProperties).setHeader("Version", 2);
        verify(messageProperties).setHeader("metadata1", "value1");
        verify(messageProperties).setType("LIVE.TEST_FEED_2");
    }

    @Test
    void testSendLiveDataNoPayload() {
        B2bFeedData data = new B2bFeedData(new B2bFeedData.Info("TEST_FEED_2", Collections.emptyList()), "{}");

        producer.send(data, "TEST_KEY_2", PlatformMessage.Type.B2B_LIVE_FEED, Map.of("metadata1", "value1"), false);

        verify(rabbitTemplate).convertAndSend(any(), eq("TEST_KEY_2"), eq(""), messagePostProcessorArgumentCaptor.capture());

        MessagePostProcessor messagePostProcessor = messagePostProcessorArgumentCaptor.getValue();
        messagePostProcessor.postProcessMessage(message);

        verify(message).getMessageProperties();
        verify(messageProperties).setHeader("Version", 2);
        verify(messageProperties).setHeader("metadata1", "value1");
        verify(messageProperties).setType("LIVE.TEST_FEED_2");
    }
}
