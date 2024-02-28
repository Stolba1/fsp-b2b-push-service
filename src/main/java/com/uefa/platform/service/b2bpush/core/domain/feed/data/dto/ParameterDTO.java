package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;

import java.util.Map;
import java.util.Objects;

public class ParameterDTO {

    private static final String NAME_PARAM = "name";
    private static final String VALUE_PARAM = "value";
    private static final String ID_AGGREGATOR_NAME_PARAM = "idAggregatorName";
    private static final String ID_AGGREGATOR_PARAMETERS_PARAM = "idAggregatorParameters";

    private final String name;

    private final String value;

    private final String idAggregatorName;

    private final Map<String, String> idAggregatorParameters;


    @JsonCreator
    public ParameterDTO(@JsonProperty(NAME_PARAM) String name,
                        @JsonProperty(VALUE_PARAM) String value,
                        @JsonProperty(ID_AGGREGATOR_NAME_PARAM) String idAggregatorName,
                        @JsonProperty(ID_AGGREGATOR_PARAMETERS_PARAM) Map<String, String> idAggregatorParameters) {

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

    /**
     * Create a new {@link ParameterDTO} DTO from a {@link Client.FeedConfiguration.Parameter} entity
     *
     * @param parameter not null
     * @return the instance
     */
    public static ParameterDTO instanceOf(Client.FeedConfiguration.Parameter parameter) {

        return new ParameterDTO(parameter.getName(),
                parameter.getValue(),
                parameter.getIdAggregatorName(),
                parameter.getIdAggregatorParameters());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterDTO that = (ParameterDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) &&
                Objects.equals(idAggregatorName, that.idAggregatorName) &&
                Objects.equals(idAggregatorParameters, that.idAggregatorParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, idAggregatorName, idAggregatorParameters);
    }

    @Override
    public String toString() {
        return "ParameterDTO{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", idAggregatorName='" + idAggregatorName + '\'' +
                ", idAggregatorParameters=" + idAggregatorParameters +
                '}';
    }
}
