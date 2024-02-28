package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class DashboardIdAggregatorDTO {

    private static final String NAME_PARAM = "name";
    private static final String PARAMETERS_PARAM = "parameters";

    private final String name;

    private final List<String> parameters;

    @JsonCreator
    public DashboardIdAggregatorDTO(@JsonProperty(NAME_PARAM) String name,
                                    @JsonProperty(PARAMETERS_PARAM) List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DashboardIdAggregatorDTO that = (DashboardIdAggregatorDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    @Override
    public String toString() {
        return "DashboardIdAggregatorDTO{" + "name='" + name + '\'' + ", parameters=" + parameters + '}';
    }
}
