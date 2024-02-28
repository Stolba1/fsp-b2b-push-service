package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.TeamIdAggregator;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardIdAggregatorDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class IdAggregatorServiceTest {

    private IdAggregatorService idAggregatorService;

    @Mock
    private TeamIdAggregator teamIdAggregator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        idAggregatorService = new IdAggregatorService(Set.of(teamIdAggregator));
    }


    @Test
    void testGetAllIdAggregators() {
        List<DashboardIdAggregatorDTO> aggregators = idAggregatorService.getAllIdAggregators();

        Assertions.assertEquals(1, aggregators.size());
        Assertions.assertEquals(teamIdAggregator.getIdAggregatorName(), aggregators.get(0).getName());
    }

    @Test
    void testGetEntityIds() {
        when(teamIdAggregator.getIds(any())).thenReturn(Set.of("1"));
        when(teamIdAggregator.hasIdAggregatorName(eq("TEAM_ID_AGGREGATOR"))).thenReturn(true);


        Set<String> result = idAggregatorService.getEntityIds(
                new Client.FeedConfiguration.Parameter("name", "value", "TEAM_ID_AGGREGATOR", Map.of("1", "1")));

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains("1"));
    }

    @Test
    void testGetEntityIdsWrongParameter() {
        Set<String> result = idAggregatorService.getEntityIds(
                new Client.FeedConfiguration.Parameter("name", "value", null, null));

        Assertions.assertEquals(0, result.size());
    }

}
