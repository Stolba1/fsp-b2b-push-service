package com.uefa.platform.service.b2bpush.core.domain.archive;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MessageArchive {

    private final MessageArchiveRepository repository;

    private final MessageArchiveEntity archiveEntity;

    private String sentContent;

    private Instant sentTimestamp;

    private final Map<String, Object> tags;

    private MessageArchiveEntity.Status status;

    private String exceptionMessage;

    private Instant deleteDate;

    private String feedName;
    private FeedType feedType;

    public MessageArchive(MessageArchiveRepository repository, MessageArchiveEntity archiveEntity) {
        this.repository = repository;
        this.archiveEntity = archiveEntity;
        tags = new HashMap<>();
    }

    public MessageArchive withStatus(MessageArchiveEntity.Status status) {
        this.status = status;
        return this;
    }

    public MessageArchive withException(String exception) {
        this.exceptionMessage = exception;
        return this;
    }

    public MessageArchive withTag(String key, Object data) {
        this.tags.put(key, data);
        return this;
    }

    public MessageArchive withSentContent(String sentContent) {
        this.sentContent = sentContent;
        return this;
    }

    public MessageArchive withSentTimestamp(Instant sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
        return this;
    }

    public MessageArchive withDeleteDate(Instant deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    public MessageArchive withFeedName(String feedName) {
        this.feedName = feedName;
        return this;
    }

    public MessageArchive withFeedType(FeedType feedType) {
        this.feedType = feedType;
        return this;
    }

    public String getArchiveId() {
        return archiveEntity.getId();
    }

    public Status getStatus() {
        return status;
    }

    public void save() {
        final MessageArchiveEntity updatedArchive = new MessageArchiveEntity(archiveEntity.getId(),
                archiveEntity.getReceivedTimestamp(),
                sentTimestamp,
                archiveEntity.getProvider(),
                archiveEntity.getReceivedContent(),
                sentContent,
                status,
                exceptionMessage,
                tags,
                deleteDate,
                feedName,
                feedType);

        repository.save(updatedArchive);
    }
}
