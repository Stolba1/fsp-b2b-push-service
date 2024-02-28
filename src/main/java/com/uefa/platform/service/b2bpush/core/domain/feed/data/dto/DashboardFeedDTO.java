package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class DashboardFeedDTO {

    private static final String ID_PARAM = "id";
    private static final String CODE_PARAM = "code";
    private static final String STATUS_PARAM = "status";
    public static final String URL_PARAM = "url";
    private static final String PARAMETERS_PARAM = "parameters";
    private static final String BOOTSTRAP_PARAMETERS_PARAM = "bootstrapParameters";
    private static final String LAST_PROCESSING_TIME_PARAM = "lastProcessingTime";
    public static final String PROCESS_EVERY_MINUTES_PARAM = "processEveryMinutes";
    public static final String TYPE_PARAM = "type";
    public static final String LIVE_DATA_TYPE_PARAM = "liveDataType";
    public static final String RETENTION_DAYS = "retentionDays";

    private final String id;

    @Valid
    @Pattern(regexp = "^[A-Z0-9_]*$", message = "'Code' field does not match expected pattern: ^[A-Z0-9_]*$")
    private final String code;

    private final Status status;

    private final String url;

    private final Set<String> parameters;

    private final Set<String> bootstrapParameters;

    private final Instant lastProcessingTime;

    private final Integer processEveryMinutes;

    private final Integer retentionDays;

    @Valid
    @NotNull(message = "Feed 'type' could not be null")
    private final FeedType type;

    private final LiveFeedDataType liveDataType;

    @JsonCreator
    public DashboardFeedDTO(@JsonProperty(ID_PARAM) String id,
                            @JsonProperty(CODE_PARAM) String code,
                            @JsonProperty(STATUS_PARAM) Status status,
                            @JsonProperty(URL_PARAM) String url,
                            @JsonProperty(PARAMETERS_PARAM) Set<String> parameters,
                            @JsonProperty(BOOTSTRAP_PARAMETERS_PARAM) Set<String> bootstrapParameters,
                            @JsonProperty(LAST_PROCESSING_TIME_PARAM) Instant lastProcessingTime,
                            @JsonProperty(PROCESS_EVERY_MINUTES_PARAM) Integer processEveryMinutes,
                            @JsonProperty(TYPE_PARAM) FeedType type,
                            @JsonProperty(LIVE_DATA_TYPE_PARAM) LiveFeedDataType liveDataType,
                            @JsonProperty(RETENTION_DAYS) Integer retentionDays) {
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

    public Integer getRetentionDays() { return retentionDays;}

    /**
     * Create a new {@link DashboardFeedDTO} DTO from a {@link Feed} entity
     *
     * @param feed not null
     * @return the instance
     */
    public static DashboardFeedDTO instanceOf(Feed feed) {
        return new DashboardFeedDTO(feed.getId(),
                feed.getCode(),
                feed.getStatus(),
                feed.getUrl(),
                feed.getParameters(),
                feed.getBootstrapParameters(),
                feed.getLastProcessingTime(),
                feed.getProcessEveryMinutes(),
                feed.getType(),
                feed.getLiveDataType(),
                feed.getRetentionDays());
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DashboardFeedDTO that = (DashboardFeedDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(code, that.code) && status == that.status &&
                Objects.equals(url, that.url) && Objects.equals(parameters, that.parameters) &&
                Objects.equals(bootstrapParameters, that.bootstrapParameters) && Objects.equals(lastProcessingTime, that.lastProcessingTime) &&
                Objects.equals(processEveryMinutes, that.processEveryMinutes) && Objects.equals(retentionDays, that.retentionDays) &&
                type == that.type && liveDataType == that.liveDataType;
    }

    @Override public int hashCode() {
        return Objects.hash(id, code, status, url, parameters, bootstrapParameters, lastProcessingTime, processEveryMinutes, retentionDays, type, liveDataType);
    }
    @Override public String toString() {
        return "DashboardFeedDTO{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", parameters=" + parameters +
                ", bootstrapParameters=" + bootstrapParameters +
                ", lastProcessingTime=" + lastProcessingTime +
                ", processEveryMinutes=" + processEveryMinutes +
                ", retentionDays=" + retentionDays +
                ", type=" + type +
                ", liveDataType=" + liveDataType +
                '}';
    }
}
