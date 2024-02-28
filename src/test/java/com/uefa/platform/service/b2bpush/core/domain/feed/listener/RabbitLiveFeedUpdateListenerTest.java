package com.uefa.platform.service.b2bpush.core.domain.feed.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.LiveFeedMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bLiveFeedData;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.LiveFeedHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.uefa.platform.service.b2bpush.TestUtils.loadResource;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Component
class RabbitLiveFeedUpdateListenerTest {

    private static final byte[] MSG_BODY = {1, 2, 3};

    private RabbitLiveFeedUpdateListener listener;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private LiveFeedHandlerService liveFeedHandlerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        listener = new RabbitLiveFeedUpdateListener(liveFeedHandlerService, objectMapper);
    }

    @Test
    void testReceiveMessage() throws IOException {
        final String messageString = loadResource("/inputSample/live-feed-data.json");
        final B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), messageString);
        final LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.COMPETITION_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        when(objectMapper.readValue(MSG_BODY, PlatformMessage.class))
                .thenReturn(message);
        listener.onMessage(mockMessage());
        verify(liveFeedHandlerService, times(1)).handleMessage(message, null);
    }

    @Test
    void testReceiveMessageWithHeader() throws IOException {
        final String messageString = loadResource("/inputSample/live-feed-data.json");
        final B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), messageString);
        final LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.COMPETITION_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        when(objectMapper.readValue(MSG_BODY, PlatformMessage.class))
                .thenReturn(message);

        String clientFeedConfigurationId = "123456789";
        Message mockMessage = mockMessage();
        mockMessage.getMessageProperties().setHeader("clientFeedConfigurationId", clientFeedConfigurationId);
        listener.onMessage(mockMessage);
        verify(liveFeedHandlerService, times(1)).handleMessage(message, clientFeedConfigurationId);
    }

    @Test
    void testReceiveInvalidMessage() throws IOException {
        String clientConfigurationId = "123456789";

        final String messageString = loadResource("/inputSample/live-feed-data.json");
        final B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), messageString);
        final LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.COMPETITION_STATISTICS_SERVICE,
                PlatformMessage.Type.DOMESTIC_DELETE_ENTITY,
                data);
        when(objectMapper.readValue(MSG_BODY, PlatformMessage.class))
                .thenReturn(message);
        listener.onMessage(mockMessage());
        verify(liveFeedHandlerService, times(0)).handleMessage(message, clientConfigurationId);
    }

    private Message mockMessage() {
        return new Message(MSG_BODY, new MessageProperties());
    }

}
