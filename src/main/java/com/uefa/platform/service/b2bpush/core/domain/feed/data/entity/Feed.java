package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Document(collection = "feeds")
public class Feed {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_URL = "url";
    public static final String FIELD_PARAMETERS = "parameters";
    public static final String FIELD_BOOTSTRAP_PARAMETERS = "bootstrapParameters";
    public static final String FIELD_LAST_PROCESSING_TIME = "lastProcessingTime";
    public static final String FIELD_PROCESS_EVERY_MINUTES = "processEveryMinutes";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_LIVE_DATA_TYPE = "liveDataType";
    public static final String FIELD_RETENTION_DAYS = "retentionDays";

    @Id
    private final String id;

    @Field(FIELD_CODE)
    @Indexed(unique = true)
    private final String code;

    @Field(FIELD_STATUS)
    private final Status status;

    @Field(FIELD_URL)
    private final String url;

    @Field(FIELD_PARAMETERS)
    private final Set<String> parameters;

    @Field(FIELD_BOOTSTRAP_PARAMETERS)
    private final Set<String> bootstrapParameters;

    @Field(FIELD_LAST_PROCESSING_TIME)
    private final Instant lastProcessingTime;

    @Field(FIELD_PROCESS_EVERY_MINUTES)
    private final Integer processEveryMinutes;

    @Field(FIELD_TYPE)
    private final FeedType type;

    @Field(FIELD_LIVE_DATA_TYPE)
    private final LiveFeedDataType liveDataType;

    @Field(FIELD_RETENTION_DAYS)
    private final Integer retentionDays;

    public Feed(String id, String code, Status status, String url, Set<String> parameters,
                Set<String> bootstrapParameters, Instant lastProcessingTime,
                Integer processEveryMinutes, FeedType type, LiveFeedDataType liveDataType, Integer retentionDays) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.url = url;
        this.parameters = parameters;
        this.bootstrapParameters = bootstrapParameters;
        this.lastProcessingTime = lastProcessingTime;
        this.processEveryMinutes = processEveryMinutes;
        this.type = type;
        this.liveDataType = liveDataType;
        this.retentionDays = retentionDays;
    }

    public static Feed instanceOf(DashboardFeedDTO feedDTO, Instant lastProcessingTime) {
        return new Feed(null,
                feedDTO.getCode(),
                feedDTO.getStatus(),
                feedDTO.getUrl(),
                feedDTO.getParameters(),
                feedDTO.getBootstrapParameters(),
                lastProcessingTime,
                feedDTO.getProcessEveryMinutes(),
                feedDTO.getType(),
                feedDTO.getLiveDataType(),
                feedDTO.getRetentionDays());
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Status getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getParameters() {
        return parameters;
    }

    public Set<String> getBootstrapParameters() {
        return bootstrapParameters;
    }

    public Instant getLastProcessingTime() {
        return lastProcessingTime;
    }

    public Integer getProcessEveryMinutes() {
        return processEveryMinutes;
    }

    public FeedType getType() {
        return type;
    }

    public LiveFeedDataType getLiveDataType() {
        return liveDataType;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feed feed = (Feed) o;
        return Objects.equals(id, feed.id) && Objects.equals(code, feed.code) && status == feed.status &&
                Objects.equals(url, feed.url) && Objects.equals(parameters, feed.parameters) &&
                Objects.equals(bootstrapParameters, feed.bootstrapParameters) && Objects.equals(lastProcessingTime, feed.lastProcessingTime) &&
                Objects.equals(processEveryMinutes, feed.processEveryMinutes) && type == feed.type && liveDataType == feed.liveDataType &&
                Objects.equals(retentionDays, feed.retentionDays);
    }

    @Override public int hashCode() {
        return Objects.hash(id, code, status, url, parameters, bootstrapParameters, lastProcessingTime, processEveryMinutes, type, liveDataType, retentionDays);
    }

    @Override public String toString() {
        return "Feed{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", parameters=" + parameters +
                ", bootstrapParameters=" + bootstrapParameters +
                ", lastProcessingTime=" + lastProcessingTime +
                ", processEveryMinutes=" + processEveryMinutes +
                ", type=" + type +
                ", liveDataType=" + liveDataType +
                ", retentionDays=" + retentionDays +
                '}';
    }

    public static class FeedUpdateBuilder extends BaseUpdateBuilder {
        private FeedUpdateBuilder() {
            super();
        }

        public FeedUpdateBuilder with(Feed feed) {
            setField(FIELD_CODE, feed.getCode());
            setField(FIELD_STATUS, feed.getStatus());
            setField(FIELD_PARAMETERS, feed.getParameters());
            setField(FIELD_BOOTSTRAP_PARAMETERS, feed.getBootstrapParameters());
            setField(FIELD_URL, feed.getUrl());
            setField(FIELD_PROCESS_EVERY_MINUTES, feed.getProcessEveryMinutes());
            setField(FIELD_LAST_PROCESSING_TIME, feed.getLastProcessingTime());
            setField(FIELD_TYPE, feed.getType());
            setField(FIELD_LIVE_DATA_TYPE, feed.getLiveDataType());
            setField(FIELD_RETENTION_DAYS, feed.getRetentionDays());
            return this;
        }

        public FeedUpdateBuilder setLastProcessingTime(Instant lastProcessingTime) {
            setField(FIELD_LAST_PROCESSING_TIME, lastProcessingTime);
            return this;
        }

        public static FeedUpdateBuilder create() {
            return new FeedUpdateBuilder();
        }
    }

}
