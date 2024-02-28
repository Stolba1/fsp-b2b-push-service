package com.uefa.platform.service.b2bpush.core.domain.feed.aggregator;

import com.uefa.platform.client.competition.v2.TeamClient;
import com.uefa.platform.dto.competition.v2.TeamPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
public class TeamIdAggregatorTest {

    @Mock
    private TeamClient teamClient;

    private TeamIdAggregator service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new TeamIdAggregator(teamClient);
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
        final TeamPlayer teamPlayer1 = new TeamPlayer("teamid1", "playerid1");
        final TeamPlayer teamPlayer2 = new TeamPlayer("teamid2", "playerid2");
        final TeamPlayer teamPlayer3 = new TeamPlayer("teamid1", "playerid3");

        when(teamClient.getTeamPlayers("1", "2022", null, null, null, null, null))
                .thenReturn(Mono.just(List.of(teamPlayer1, teamPlayer2, teamPlayer3)));
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("teamid1"));
        Assertions.assertTrue(result.contains("teamid2"));
    }

    @Test
    void testGetIdsMonoError() {

        when(teamClient.getTeamPlayers("1", "2022", null, null, null, null, null))
                .thenReturn(Mono.error(new RuntimeException("Some error")));
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsMonoEmpty() {

        when(teamClient.getTeamPlayers("1", "2022", null, null, null, null, null))
                .thenReturn(Mono.empty());
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsWrongNumberOfParameters() {
        final Map<String, String> parameters = Map.of(COMPETITION_ID_PARAM, "1", SEASON_YEAR_PARAM, "2022", "someOtherParam", "valaue");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdsMissingCompetitionParameter() {
        final Map<String, String> parameters = Map.of(SEASON_YEAR_PARAM, "2022", "someOtherParam", "valaue");

        Set<String> result = service.getIds(parameters);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIdAggregatorName() {
        Assertions.assertEquals("TEAM_ID_AGGREGATOR", service.getIdAggregatorName());
    }

    @Test
    void testHasIdAggregatorName() {
        Assertions.assertTrue(service.hasIdAggregatorName("TEAM_ID_AGGREGATOR"));
        Assertions.assertFalse(service.hasIdAggregatorName("DUMMY"));
    }

}
