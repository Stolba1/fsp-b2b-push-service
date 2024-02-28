package com.uefa.platform.service.b2bpush.core.domain.matchstate.archive;


import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class MessageArchiveBuilderTest {

    @Mock
    private MessageArchiveRepository messageArchiveRepository;

    private MessageArchiveBuilder messageArchiveBuilder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        messageArchiveBuilder = new MessageArchiveBuilder(messageArchiveRepository);
    }

    @Test
    void testBuild() {
        final Instant receivedAt = Instant.now();
        final String uuid = UUID.randomUUID().toString();
        final MessageArchiveEntity entity = new MessageArchiveEntity(uuid,
                receivedAt, null, MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, "content",
                null, null, null, null, null, null, null);

        when(messageArchiveRepository.insert((MessageArchiveEntity) any())).thenReturn(entity);
        final MessageArchive result = messageArchiveBuilder.build(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, "content", Instant.now());


        Mockito.verify(messageArchiveRepository).insert(argThat((MessageArchiveEntity e) -> e.getExceptionMessage() == null &&
                e.getProvider().equals(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER) &&
                e.getReceivedContent().equals("content") &&
                e.getSentContent() == null &&
                e.getTags() == null &&
                e.getSentTimestamp() == null));

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(uuid, result.getArchiveId());
    }

    @Test
    void testBuildStaticMessageArchive() {
        final Instant sentAt = Instant.now();
        final String uuid = UUID.randomUUID().toString();
        final MessageArchiveEntity entity = new MessageArchiveEntity(uuid,
                null, sentAt, null, null,
                null, null, null, null, null, "feedName", FeedType.STATIC);

        when(messageArchiveRepository.insert((MessageArchiveEntity) any())).thenReturn(entity);
        final MessageArchive result = messageArchiveBuilder.buildStaticMessageArchive();


        Mockito.verify(messageArchiveRepository).insert(argThat((MessageArchiveEntity e) -> e.getExceptionMessage() == null &&
                e.getSentContent() == null &&
                e.getTags() == null &&
                e.getSentTimestamp() == null));

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(uuid, result.getArchiveId());
    }
}
