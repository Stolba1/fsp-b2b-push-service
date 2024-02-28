package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.EidosContent;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.eidos.EidosData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import static com.uefa.platform.dto.message.PlatformMessage.Action;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchStateUpdateListenerTest {

    private static final byte[] MSG_BODY = {1, 2, 3};

    @Mock
    private MatchStateUpdateHandler handler;
    @Mock
    private ObjectMapper platformMessageMapper;
    @InjectMocks
    private MatchStateUpdateListener listener;

    @Test
    void testOnMessage_Exception() throws IOException {
        when(platformMessageMapper.readValue(any(byte[].class), eq(PlatformMessage.class))).thenThrow(new RuntimeException());

        listener.onMessage(mockMessage());

        verifyNoInteractions(handler);
    }

    @Test
    void testInvalidMessage() throws IOException {
        EidosData data = new EidosData("id", "contentType", false, Instant.now(),
                new EidosData.Info("hlsStream", "en", "1", "TOURNAMENT", Set.of(),
                        new EidosData.Info.ContentInfo("type", "section"), null, null,
                        null, null, Instant.now(), null, null, null, null), "title", "summary");
        Mockito.when(platformMessageMapper.readValue(MSG_BODY, PlatformMessage.class))
                .thenReturn(new EidosContent(Action.EidosAdapter.UPDATE, data));

        listener.onMessage(mockMessage());

        verifyNoInteractions(handler);
    }

    @Test
    void testOnMessage() throws IOException {
        MatchStateData matchStateData = dummyData();
        MatchStateMessage platformMessage = new MatchStateMessage(Action.MatchStateProvider.UPDATE, matchStateData);
        when(platformMessageMapper.readValue(MSG_BODY, PlatformMessage.class))
                .thenReturn(platformMessage);

        listener.onMessage(mockMessage());

        verify(handler).handleUpdate(platformMessage);
    }

    private Message mockMessage() {
        return new Message(MSG_BODY, new MessageProperties());
    }

}
