package com.uefa.platform.service.b2bpush.core.domain.archive.data.entity;


import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Document(collection = "sent-message-archive")
@CompoundIndex(name = "idx_matchId", def = "{'provider': 1, 'tags.matchId': 1}")
@CompoundIndex(name = "idx_match_provider_status", def = "{ 'tags.matchId': 1, 'provider': 1, 'status': 1, 'sentTimestamp': -1 }")
@CompoundIndex(name = "idx_match_event_ids", def = "{ 'tags.matchId': 1, 'tags.eventIds': 1 }")
@CompoundIndex(name = "idx_match_sent_content", def = "{ 'tags.matchId': 1, 'sentContent': 'text', 'sentTimestamp': -1}")
public class MessageArchiveEntity {

    @Id
    private final String id;

    @Field("receivedTimestamp")
    private final Instant receivedTimestamp;

    @Field("sentTimestamp")
    private final Instant sentTimestamp;

    @Field("provider")
    private final MessageProvider provider;

    @Field("receivedContent")
    private final String receivedContent;

    @Field("sentContent")
    private final String sentContent;

    @Field("status")
    private final Status status;

    @Field("exceptionMessage")
    private final String exceptionMessage;

    @Field("tags")
    private final Map<String, Object> tags;

    @Indexed(name = "idx_deleteDate", expireAfter = "1s")
    @Field("deleteDate")
    private final Instant deleteDate;

    @Field("feedName")
    private final String feedName;

    @Field("feedType")
    private final FeedType feedType;

    @PersistenceCreator
    public MessageArchiveEntity(String id, Instant receivedTimestamp, Instant sentTimestamp, MessageProvider provider,
                                String receivedContent, String sentContent, Status status, String exceptionMessage, Map<String, Object> tags,
                                Instant deleteDate, String feedName, FeedType feedType) {
        this.id = id;
        this.receivedTimestamp = receivedTimestamp;
        this.sentTimestamp = sentTimestamp;
        this.provider = provider;
        this.receivedContent = receivedContent;
        this.sentContent = sentContent;
        this.status = status;
        this.exceptionMessage = exceptionMessage;
        this.tags = tags;
        this.deleteDate = deleteDate;
        this.feedName = feedName;
        this.feedType = feedType;
    }

    public String getId() {
        return id;
    }

    public Instant getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public Instant getSentTimestamp() {
        return sentTimestamp;
    }

    public MessageProvider getProvider() {
        return provider;
    }

    public String getReceivedContent() {
        return receivedContent;
    }

    public String getSentContent() {
        return sentContent;
    }

    public Status getStatus() {
        return status;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public Instant getDeleteDate() {
        return deleteDate;
    }

    public String getFeedName() { return feedName; }

    public FeedType getFeedType() { return feedType; }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageArchiveEntity that = (MessageArchiveEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(receivedTimestamp, that.receivedTimestamp) &&
                Objects.equals(sentTimestamp, that.sentTimestamp) && provider == that.provider &&
                Objects.equals(receivedContent, that.receivedContent) && Objects.equals(sentContent, that.sentContent) &&
                status == that.status && Objects.equals(exceptionMessage, that.exceptionMessage) && Objects.equals(tags, that.tags) &&
                Objects.equals(deleteDate, that.deleteDate) && Objects.equals(feedName, that.feedName) &&
                Objects.equals(feedType, that.feedType);
    }

    @Override public int hashCode() {
        return Objects.hash(id, receivedTimestamp, sentTimestamp, provider, receivedContent, sentContent, status, exceptionMessage, tags, deleteDate, feedName,
                feedType);
    }

    @Override public String toString() {
        return "MessageArchiveEntity{" +
                "id='" + id + '\'' +
                ", receivedTimestamp=" + receivedTimestamp +
                ", sentTimestamp=" + sentTimestamp +
                ", provider=" + provider +
                ", receivedContent='" + receivedContent + '\'' +
                ", sentContent='" + sentContent + '\'' +
                ", status=" + status +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", tags=" + tags +
                ", deleteDate=" + deleteDate +
                ", feedName='" + feedName + '\'' +
                ", feedType='" + feedType + '\'' +
                '}';
    }

    public enum Status {
        SUCCESS, ERROR
    }

    public enum MessageProvider {
        MATCH_STATE_PROVIDER, MATCH_STATISTICS_SERVICE, COMPETITION_STATISTICS_SERVICE, TRANSLATION_SERVICE
    }

}
