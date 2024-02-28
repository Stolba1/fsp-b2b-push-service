package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class FeedConfigurationDTO {

    private static final String ID_PARAM = "id";
    private static final String FEED_ID_PARAM = "feedId";
    private static final String PARAMETERS_PARAM = "parameters";
    private static final String STATUS_PARAM = "status";
    private static final String LAST_SENT_TIME_PARAM = "lastSentTime";
    private static final String SEND_PAYLOAD_TO_CLIENT_PARAM = "payloadSharedToClient";

    private final String id;

    private final String feedId;

    private final List<ParameterDTO> parameters;

    private final Status status;

    private final Instant lastSentTime;

    private final Boolean payloadSharedToClient;

    @JsonCreator
    public FeedConfigurationDTO(@JsonProperty(ID_PARAM) String id,
                                @JsonProperty(FEED_ID_PARAM) String feedId,
                                @JsonProperty(PARAMETERS_PARAM) List<ParameterDTO> parameters,
                                @JsonProperty(STATUS_PARAM) Status status,
                                @JsonProperty(LAST_SENT_TIME_PARAM) Instant lastSentTime,
                                @JsonProperty(SEND_PAYLOAD_TO_CLIENT_PARAM) Boolean payloadSharedToClient) {
        this.id = id;
        this.feedId = feedId;
        this.parameters = parameters;
        this.status = status;
        this.lastSentTime = lastSentTime;
        this.payloadSharedToClient = payloadSharedToClient;
    }

    public String getId() {
        return id;
    }

    public String getFeedId() {
        return feedId;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLastSentTime() {
        return lastSentTime;
    }

    public Boolean isPayloadSharedToClient() {
        return payloadSharedToClient;
    }

    /**
     * Create a new {@link FeedConfigurationDTO} DTO from a {@link Client.FeedConfiguration} entity
     *
     * @param feedConfiguration not null
     * @return the instance
     */
    public static FeedConfigurationDTO instanceOf(Client.FeedConfiguration feedConfiguration) {
        return new FeedConfigurationDTO(feedConfiguration.getId(),
                feedConfiguration.getFeedId(),
                convertParameter(feedConfiguration.getParameters()),
                feedConfiguration.getStatus(),
                feedConfiguration.getLastSentTime(),
                feedConfiguration.isPayloadSharedToClient());
    }

    private static List<ParameterDTO> convertParameter(List<Client.FeedConfiguration.Parameter> parameters) {
        return parameters.stream().map(ParameterDTO::instanceOf)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedConfigurationDTO that = (FeedConfigurationDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(feedId, that.feedId) &&
                Objects.equals(parameters, that.parameters) &&
                status == that.status &&
                Objects.equals(lastSentTime, that.lastSentTime) &&
                Objects.equals(payloadSharedToClient, that.payloadSharedToClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feedId, parameters, status, lastSentTime, payloadSharedToClient);
    }

    @Override
    public String toString() {
        return "FeedConfigurationDTO{" +
                "id='" + id + '\'' +
                ", feedId='" + feedId + '\'' +
                ", parameters=" + parameters +
                ", status=" + status +
                ", lastSentTime=" + lastSentTime +
                ", payloadSharedToClient=" + payloadSharedToClient +
                '}';
    }
}
