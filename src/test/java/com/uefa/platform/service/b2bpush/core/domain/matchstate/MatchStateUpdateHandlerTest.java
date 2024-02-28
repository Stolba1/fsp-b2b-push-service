package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.B2bMatchStateMessage;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.MatchStateDataConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyData;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyDataWithOfficials;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchStateUpdateHandlerTest {

    @Mock
    private MatchStateMessageProducer producer;

    @Mock
    private MatchStateDataConverter converter;

    @Mock
    private MessageArchiveBuilder messageArchiveBuilder;

    private ObjectMapper objectMapper;

    private MatchStateUpdateHandler handler;

    @Captor
    private ArgumentCaptor<MessageArchiveEntity> messageArchiveCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.objectMapper = new ObjectMapper();
        handler = new MatchStateUpdateHandler(producer, converter, messageArchiveBuilder, objectMapper);
    }

    @Test
    void testHandleUpdate() throws JsonProcessingException {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);

        when(messageArchiveBuilder.build(any(), anyString(), any())).thenReturn(
                new MessageArchive(messageArchiveRepository, new MessageArchiveEntity(UUID.randomUUID().toString(),
                        Instant.now(), null, MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, null,
                        null, null, null, null, null, null, null)));

        MatchStateData matchStateData = dummyData();
        B2bMatchStateData b2bData = new B2bMatchStateData(matchStateData, null, null, null);

        // comment the enrichment of the b2b push messages as part of the following story https://jira.uefa.com/browse/FSP-2068
        // Mockito.when(converter.convert(matchStateData)).thenReturn(b2bData);
        final MatchStateMessage message = new MatchStateMessage(PlatformMessage.Action.MatchStateProvider.UPDATE, matchStateData);
        handler.handleUpdate(message);

        B2bMatchStateMessage expected = new B2bMatchStateMessage(new B2bMatchStateData(matchStateData.copyWithoutProviderData(), null, null, null));
        Mockito.verify(producer).send(expected);

        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER), eq(objectMapper.writeValueAsString(message)), any());
        Mockito.verify(messageArchiveRepository).save(messageArchiveCaptor.capture());
        MessageArchiveEntity archive = messageArchiveCaptor.getValue();
        Assertions.assertNotNull(archive);
        Assertions.assertNotNull(archive.getDeleteDate());
        Assertions.assertTrue(archive.getDeleteDate().isAfter(Instant.now().plus(29, ChronoUnit.DAYS)));
        Assertions.assertTrue(archive.getDeleteDate().isBefore(Instant.now().plus(31, ChronoUnit.DAYS)));
    }

    @Test
    void testHandleUpdateOfficials() throws JsonProcessingException {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);

        when(messageArchiveBuilder.build(any(), anyString(), any())).thenReturn(
                new MessageArchive(messageArchiveRepository, new MessageArchiveEntity(UUID.randomUUID().toString(),
                        Instant.now(), null, MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, null,
                        null, null, null, null, null, null, null)));

        MatchStateData matchStateData = dummyDataWithOfficials();
        B2bMatchStateData b2bData = new B2bMatchStateData(matchStateData, null, null, null);

        // comment the enrichment of the b2b push messages as part of the following story https://jira.uefa.com/browse/FSP-2068
        // Mockito.when(converter.convert(matchStateData)).thenReturn(b2bData);
        final MatchStateMessage message = new MatchStateMessage(PlatformMessage.Action.MatchStateProvider.UPDATE, matchStateData);
        handler.handleUpdate(message);

        B2bMatchStateMessage expected = new B2bMatchStateMessage(new B2bMatchStateData(matchStateData.copyWithoutProviderData(), null, null, null));
        Mockito.verify(producer).send(expected);

        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER), eq(objectMapper.writeValueAsString(message)), any());
        Mockito.verify(messageArchiveRepository).save(any());
    }

    @Test
    void testHandleUpdateWithError() throws JsonProcessingException {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        final ArgumentCaptor<MessageArchiveEntity> argumentCaptor = ArgumentCaptor.forClass(MessageArchiveEntity.class);
        when(messageArchiveRepository.save(argumentCaptor.capture())).thenReturn(null);

        when(messageArchiveBuilder.build(any(), anyString(), any())).thenReturn(
                new MessageArchive(messageArchiveRepository, new MessageArchiveEntity(UUID.randomUUID().toString(),
                        Instant.now(), null, MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, null,
                        null, null, null, null, null, null, null)));

        MatchStateData matchStateData = dummyData();
        B2bMatchStateData b2bData = new B2bMatchStateData(matchStateData, null, null, null);

        // comment the enrichment of the b2b push messages as part of the following story https://jira.uefa.com/browse/FSP-2068
        // Mockito.when(converter.convert(matchStateData)).thenReturn(b2bData);
        final MatchStateMessage message = new MatchStateMessage(PlatformMessage.Action.MatchStateProvider.UPDATE, matchStateData);
        doThrow(RuntimeException.class).when(producer).send(any());
        handler.handleUpdate(message);

        B2bMatchStateMessage expected = new B2bMatchStateMessage(new B2bMatchStateData(matchStateData.copyWithoutProviderData(), null, null, null));
        Mockito.verify(producer).send(expected);

        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER), eq(objectMapper.writeValueAsString(message)), any());
        Mockito.verify(messageArchiveRepository).save(any());
        final MessageArchiveEntity entity = argumentCaptor.getValue();
        Assertions.assertNotNull(entity.getExceptionMessage());
        Assertions.assertEquals(MessageArchiveEntity.Status.ERROR, entity.getStatus());
    }
}
