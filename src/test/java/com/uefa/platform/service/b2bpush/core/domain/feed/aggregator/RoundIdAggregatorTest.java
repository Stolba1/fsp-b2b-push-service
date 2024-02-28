package com.uefa.platform.service.b2bpush.core.domain.feed.aggregator;

import com.uefa.platform.client.competition.v2.RoundClient;
import com.uefa.platform.dto.competition.v2.CompetitionPhase;
import com.uefa.platform.dto.competition.v2.Round;
import com.uefa.platform.dto.competition.v2.Round.Mode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.AggregatorConstants.COMPETITION_ID_PARAM;
import static com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.AggregatorConstants.SEASON_YEAR_PARAM;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundIdAggregatorTest {

    @Mock
    private RoundClient roundClient;

    private RoundIdAggregator service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new RoundIdAggregator(roundClient);
    }

    @Test
    void testGetRequiredParameters() {
        List<String> parameters = service.getRequiredParameters();
        Assertions.assertEquals(2, parameters.size());
        Assertions.assertEquals(COMPETITION_ID_PARAM, parameters.get(0));
        Assertions.assertEquals(SEASON_YEAR_PARAM, parameters.get(1));

    }

    @Test
    void testGetIds() {
        final Round round1 = new Round("1", 1, true, Mode.GROUP, null, null, CompetitionPhase.QUALIFYING,
                "1", "2022", null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null);
        final Round round2 = new Round("2", 1, true, Mode.FINAL, null, null, CompetitionPhase.TOURNAMENT,
                "1", "2022", null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null);

        when(roundClient.getRounds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(List.of(round1, round2)));
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("1"));
        Assertions.assertTrue(result.contains("2"));
    }

    @Test
    void testGetIdsMonoError() {

        when(roundClient.getRounds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new RuntimeException("Some error")));
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsMonoEmpty() {

        when(roundClient.getRounds(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.empty());
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsWrongNumberOfParameters() {
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022", "someOtherParam", "value");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsMissingCompetitionParameter() {
        final Map<String, String> parameters = Map.of(SEASON_YEAR_PARAM, "2022", "someOtherParam", "value");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdAggregatorName() {
        Assertions.assertEquals("ROUND_ID_AGGREGATOR", service.getIdAggregatorName());
    }

    @Test
    void testHasIdAggregatorName() {
        Assertions.assertTrue(service.hasIdAggregatorName("ROUND_ID_AGGREGATOR"));
        Assertions.assertFalse(service.hasIdAggregatorName("DUMMY"));
    }

}
