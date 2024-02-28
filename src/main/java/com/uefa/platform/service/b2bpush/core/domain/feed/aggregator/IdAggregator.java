package com.uefa.platform.service.b2bpush.core.domain.feed.aggregator;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IdAggregator {

    @NotNull
    List<String> getRequiredParameters();

    @NotNull
    Set<String> getIds(Map<String, String> parameters);

    @NotNull
    String getIdAggregatorName();

    @NotNull
    boolean hasIdAggregatorName(String idAggregatorName);
}
