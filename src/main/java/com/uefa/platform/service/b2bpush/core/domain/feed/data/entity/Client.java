package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.FeedConfigurationDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.ParameterDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Document(collection = "clients")
public class Client {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_ROUTING_KEY = "routingKey";
    public static final String FIELD_CONFIGURATIONS = "configurations";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String FIELD_EVENT_PACKAGE = "eventPackage";

    @Id
    private final String id;

    @Field(FIELD_NAME)
    private final String name;

    @Field(FIELD_ROUTING_KEY)
    @Indexed(unique = true)
    private final String routingKey;

    @Field(FIELD_CONFIGURATIONS)
    private final List<FeedConfiguration> configurations;

    @Field(FIELD_STATUS)
    private final Status status;

    @Field(FIELD_LAST_UPDATE_TIME)
    private final Instant lastUpdateTime;

    @Field(FIELD_EVENT_PACKAGE)
    private final EventPackage eventPackage;

    public Client(String id, String name, String routingKey, List<FeedConfiguration> configurations, Status status, Instant lastUpdateTime,
                  EventPackage eventPackage) {
        this.id = id;
        this.name = name;
        this.routingKey = routingKey;
        this.configurations = configurations;
        this.status = status;
        this.lastUpdateTime = lastUpdateTime;
        this.eventPackage = eventPackage;
    }

    public static Client instanceOf(DashboardClientDTO clientDTO) {
        List<FeedConfiguration> feedConfigurations = clientDTO.getConfigurations()
                .stream().map(FeedConfiguration::instanceOf)
                .toList();
        return new Client(null,
                clientDTO.getName(),
                clientDTO.getRoutingKey(),
                feedConfigurations,
                clientDTO.getStatus(),
                clientDTO.getLastUpdateTime(),
                clientDTO.getEventPackage()
        );
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

    public List<FeedConfiguration> getConfigurations() {
        return configurations;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public EventPackage getEventPackage() {
        return eventPackage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Client client = (Client) o;
        return Objects.equals(id, client.id) && Objects.equals(name, client.name) &&
                Objects.equals(routingKey, client.routingKey) &&
                Objects.equals(configurations, client.configurations) && status == client.status &&
                Objects.equals(lastUpdateTime, client.lastUpdateTime) &&
                Objects.equals(eventPackage, client.eventPackage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, routingKey, configurations, status, lastUpdateTime, eventPackage);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", configurations=" + configurations +
                ", status=" + status +
                ", lastUpdateTime=" + lastUpdateTime +
                ", eventPackage=" + eventPackage +
                '}';
    }

    public static class FeedConfiguration {

        public static final String FIELD_ID = "id";
        public static final String FIELD_FEED_ID = "feedId";
        public static final String FIELD_PARAMETERS = "parameters";
        public static final String FIELD_HASH = "hash";
        public static final String FIELD_LAST_SENT_TIME = "lastSentTime";
        public static final String FIELD_STATUS = "status";
        public static final String PAYLOAD_SHARED_TO_CLIENT = "payloadSharedToClient";

        @Field(FIELD_ID)
        private final String id;

        @Field(FIELD_FEED_ID)
        private final String feedId;

        @Field(FIELD_PARAMETERS)
        private final List<Parameter> parameters;

        @Field(FIELD_HASH)
        private final String hash;

        @Field(FIELD_LAST_SENT_TIME)
        private final Instant lastSentTime;

        @Field(FIELD_STATUS)
        private final Status status;

        @Field(PAYLOAD_SHARED_TO_CLIENT)
        private final Boolean payloadSharedToClient;

        public FeedConfiguration(String id, String feedId, List<Parameter> parameters, String hash, Instant lastSentTime, Status status,
                                 Boolean payloadSharedToClient) {
            this.id = id;
            this.feedId = feedId;
            this.parameters = parameters;
            this.hash = hash;
            this.lastSentTime = lastSentTime;
            this.status = status;
            this.payloadSharedToClient = payloadSharedToClient;
        }

        public FeedConfiguration withParameters(List<Parameter> parameters) {
            return new FeedConfiguration(this.id, this.feedId, parameters, this.hash, this.lastSentTime, this.status, this.payloadSharedToClient);
        }

        public FeedConfiguration withFeedId(String feedId) {
            return new FeedConfiguration(this.id, feedId, this.parameters, this.hash, this.lastSentTime, this.status, this.payloadSharedToClient);
        }

        public FeedConfiguration withPayloadSharedToClient(boolean payloadSharedToClient) {
            return new FeedConfiguration(this.id, this.feedId, this.parameters, this.hash, this.lastSentTime, this.status, payloadSharedToClient);
        }

        public static FeedConfiguration instanceOf(FeedConfigurationDTO feedConfigurationDTO) {
            List<Parameter> parameters = feedConfigurationDTO.getParameters()
                    .stream().map(Parameter::instanceOf)
                    .toList();
            return new FeedConfiguration(feedConfigurationDTO.getId() != null ? feedConfigurationDTO.getId() : UUID.randomUUID().toString(),
                    feedConfigurationDTO.getFeedId(),
                    parameters,
                    null,
                    null,
                    feedConfigurationDTO.getStatus(),
                    feedConfigurationDTO.isPayloadSharedToClient()
            );
        }

        public String getId() {
            return id;
        }

        public String getFeedId() {
            return feedId;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public String getHash() {
            return hash;
        }

        public Instant getLastSentTime() {
            return lastSentTime;
        }

        public Status getStatus() {
            return status;
        }

        public Boolean isPayloadSharedToClient() {
            return payloadSharedToClient;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FeedConfiguration that = (FeedConfiguration) o;
            return Objects.equals(id, that.id) && Objects.equals(feedId, that.feedId) &&
                    Objects.equals(parameters, that.parameters) && Objects.equals(hash, that.hash) &&
                    Objects.equals(lastSentTime, that.lastSentTime) && status == that.status &&
                    Objects.equals(payloadSharedToClient, that.payloadSharedToClient);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, feedId, parameters, hash, lastSentTime, status, payloadSharedToClient);
        }

        @Override
        public String toString() {
            return "FeedConfiguration{" +
                    "id='" + id + '\'' +
                    ", feedId='" + feedId + '\'' +
                    ", parameters=" + parameters +
                    ", hash='" + hash + '\'' +
                    ", lastSentTime=" + lastSentTime +
                    ", status=" + status +
                    ", payloadSharedToClient=" + payloadSharedToClient +
                    '}';
        }

        public static class Parameter {

            public static final String FIELD_NAME = "name";
            public static final String FIELD_VALUE = "value";
            public static final String ID_AGGREGATOR_NAME_VALUE = "idAggregatorName";
            public static final String ID_AGGREGATOR_PARAMETERS_VALUE = "idAggregatorParameters";

            @Field(FIELD_NAME)
            private final String name;

            @Field(FIELD_VALUE)
            private final String value;

            @Field(ID_AGGREGATOR_NAME_VALUE)
            private final String idAggregatorName;

            @Field(ID_AGGREGATOR_PARAMETERS_VALUE)
            private final Map<String, String> idAggregatorParameters;


            public Parameter(String name, String value, String idAggregatorName, Map<String, String> idAggregatorParameters) {
                this.name = name;
                this.value = value;
                this.idAggregatorName = idAggregatorName;
                this.idAggregatorParameters = idAggregatorParameters;
            }

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }

            public String getIdAggregatorName() {
                return idAggregatorName;
            }

            public Map<String, String> getIdAggregatorParameters() {
                return idAggregatorParameters;
            }

            public static Parameter instanceOf(ParameterDTO parameterDTO) {
                return new Parameter(parameterDTO.getName(),
                        parameterDTO.getValue(),
                        parameterDTO.getIdAggregatorName(),
                        parameterDTO.getIdAggregatorParameters());
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Parameter parameter = (Parameter) o;
                return Objects.equals(name, parameter.name) &&
                        Objects.equals(value, parameter.value) &&
                        Objects.equals(idAggregatorName, parameter.idAggregatorName) &&
                        Objects.equals(idAggregatorParameters, parameter.idAggregatorParameters);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, value, idAggregatorName, idAggregatorParameters);
            }

            @Override
            public String toString() {
                return "Parameter{" +
                        "name='" + name + '\'' +
                        ", value='" + value + '\'' +
                        ", idAggregatorName='" + idAggregatorName + '\'' +
                        ", idAggregatorParameters=" + idAggregatorParameters +
                        '}';
            }
        }

    }

    public static class ClientUpdateBuilder extends BaseUpdateBuilder {
        private ClientUpdateBuilder() {
            super();
        }

        public ClientUpdateBuilder with(Client newClient, List<FeedConfiguration> existingConfigurations) {
            setField(FIELD_NAME, newClient.getName());
            setField(FIELD_ROUTING_KEY, newClient.getRoutingKey());
            setField(FIELD_CONFIGURATIONS, getUpdatedFeedConfigurations(newClient, existingConfigurations));
            setField(FIELD_LAST_UPDATE_TIME, Instant.now());
            setField(FIELD_EVENT_PACKAGE, newClient.getEventPackage());
            return this;
        }

        @NotNull
        private List<FeedConfiguration> getUpdatedFeedConfigurations(Client newClient, List<FeedConfiguration> existingConfigurations) {
            List<FeedConfiguration> configurationsToBeStored = new ArrayList<>();
            newClient.getConfigurations().forEach(config -> {
                Optional<FeedConfiguration> feedConfiguration = existingConfigurations.stream()
                        .filter(existingConfig -> existingConfig.getId().equals(config.getId()))
                        .findAny();
                if (feedConfiguration.isPresent()) {
                    configurationsToBeStored.add(feedConfiguration.get()
                            .withParameters(config.getParameters())
                            .withFeedId(config.feedId)
                            .withPayloadSharedToClient(config.payloadSharedToClient));
                } else {
                    configurationsToBeStored.add(new FeedConfiguration(config.getId(), config.getFeedId(), config.getParameters(),
                            null, null, config.getStatus(), config.isPayloadSharedToClient()));
                }
            });
            return configurationsToBeStored;
        }

        public static ClientUpdateBuilder create() {
            return new ClientUpdateBuilder();
        }
    }

}
