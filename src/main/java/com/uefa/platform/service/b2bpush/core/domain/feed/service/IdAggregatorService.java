package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.IdAggregator;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardIdAggregatorDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdAggregatorService {

    private final Set<IdAggregator> idAggregators;

    public IdAggregatorService(Set<IdAggregator> idAggregators) {
        this.idAggregators = idAggregators;
    }

    public List<DashboardIdAggregatorDTO> getAllIdAggregators() {
        return idAggregators.stream().map(idAggregator ->
                        new DashboardIdAggregatorDTO(idAggregator.getIdAggregatorName(), idAggregator.getRequiredParameters()))
                .collect(Collectors.toList());
    }

    public Set<String> getEntityIds(Client.FeedConfiguration.Parameter parameter) {
        //find aggregator class and use tha correct one to get the entity IDs
        for (IdAggregator aggregator : idAggregators) {
            if (aggregator.hasIdAggregatorName(parameter.getIdAggregatorName())) {
                return aggregator.getIds(parameter.getIdAggregatorParameters());
            }
        }
        return Collections.emptySet();
    }
}
