package com.uefa.platform.service.b2bpush.core.domain.archive.data;

import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MessageArchiveBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MessageArchiveBuilder.class);

    private final MessageArchiveRepository messageArchiveRepository;

    public MessageArchiveBuilder(MessageArchiveRepository messageArchiveRepository) {
        this.messageArchiveRepository = messageArchiveRepository;
    }

    public MessageArchive build(MessageArchiveEntity.MessageProvider provider, String content, Instant receivedTimestamp) {
        final MessageArchiveEntity archiveEntity = messageArchiveRepository.insert(
                new MessageArchiveEntity(UUID.randomUUID().toString(), receivedTimestamp, null, provider, content,
                        null, null, null, null, null, null, null));
        LOG.info("{} message, stored with archiveId:{}\n{}", provider.name(), archiveEntity.getId(), content);
        return new MessageArchive(messageArchiveRepository, archiveEntity);
    }
    public MessageArchive buildStaticMessageArchive() {
        final MessageArchiveEntity archiveEntity = messageArchiveRepository.insert(
                new MessageArchiveEntity(UUID.randomUUID().toString(), null, null, null, null,
                        null, null, null, null, null, null, null));
        LOG.info("archive message stored with archiveId:{}", archiveEntity.getId());
        return new MessageArchive(messageArchiveRepository, archiveEntity);
    }
}
