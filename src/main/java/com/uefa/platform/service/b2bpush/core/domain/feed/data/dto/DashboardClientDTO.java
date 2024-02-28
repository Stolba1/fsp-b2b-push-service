package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DashboardClientDTO {

    private static final String ID_PARAM = "id";
    private static final String NAME_PARAM = "name";
    private static final String ROUTING_KEY_PARAM = "routingKey";
    private static final String CONFIGURATIONS_PARAM = "configurations";
    private static final String STATUS_PARAM = "status";
    private static final String LAST_UPDATE_TIME_PARAM = "lastUpdateTime";
    private static final String EVENT_PACKAGE = "eventPackage";

    private final String id;

    private final String name;

    private final String routingKey;

    private final Status status;

    private final List<FeedConfigurationDTO> configurations;

    private final Instant lastUpdateTime;

    private final EventPackage eventPackage;

    @JsonCreator
    public DashboardClientDTO(@JsonProperty(ID_PARAM) String id,
                              @JsonProperty(NAME_PARAM) String name,
                              @JsonProperty(ROUTING_KEY_PARAM) String routingKey,
                              @JsonProperty(STATUS_PARAM) Status status,
                              @JsonProperty(CONFIGURATIONS_PARAM) List<FeedConfigurationDTO> configurations,
                              @JsonProperty(LAST_UPDATE_TIME_PARAM) Instant lastUpdateTime,
                              @JsonProperty(EVENT_PACKAGE) EventPackage eventPackage) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.routingKey = routingKey;
        this.configurations = configurations;
        this.lastUpdateTime = lastUpdateTime;
        this.eventPackage = eventPackage;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Status getStatus() {
        return status;
    }

    public List<FeedConfigurationDTO> getConfigurations() {
        return configurations;
    }

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public EventPackage getEventPackage() {
        return eventPackage;
    }

    /**
     * Create a new {@link DashboardClientDTO} DTO from a {@link Client} entity
     *
     * @param client not null
     * @return the instance
     */
    public static DashboardClientDTO instanceOf(Client client) {
        return new DashboardClientDTO(client.getId(),
                client.getName(),
                client.getRoutingKey(),
                client.getStatus(),
                convertConfigurations(client.getConfigurations()),
                client.getLastUpdateTime(),
                Optional.ofNullable(client.getEventPackage()).orElse(EventPackage.BASIC));
    }

    private static List<FeedConfigurationDTO> convertConfigurations(List<Client.FeedConfiguration> configurations) {
        return configurations.stream().map(FeedConfigurationDTO::instanceOf)
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
        DashboardClientDTO that = (DashboardClientDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(routingKey, that.routingKey) &&
                status == that.status &&
                Objects.equals(configurations, that.configurations) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime) &&
                Objects.equals(eventPackage, that.eventPackage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, routingKey, status, configurations, lastUpdateTime, eventPackage);
    }

    @Override
    public String toString() {
        return "DashboardClientDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", status=" + status +
                ", configurations=" + configurations +
                ", lastUpdateTime=" + lastUpdateTime +
                ", eventPackage=" + eventPackage +
                '}';
    }
}
