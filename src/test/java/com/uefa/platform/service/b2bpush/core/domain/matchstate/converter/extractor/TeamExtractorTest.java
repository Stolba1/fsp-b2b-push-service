package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.client.competition.v2.TeamClient;
import com.uefa.platform.dto.competition.v2.Team;
import com.uefa.platform.dto.match.v2.Event;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchInfo;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.CompetitionTestModels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.uefa.platform.service.b2bpush.MatchStateTestModels.createdEvent;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyData;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyMatchInfo;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyMatchState;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.eventWithActors;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.update;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.updateLineup;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamExtractorTest {

    @Mock
    private TeamClient teamClient;

    @InjectMocks
    private TeamExtractor teamExtractor;

    @Test
    void testExtractNoIds() {
        MatchStateData data = dummyData();

        Mono<Map<String, Team>> result = teamExtractor.extract(data);

        verify(teamClient, never()).getByTeamIds(any());
        StepVerifier.create(result).expectNext(Map.of()).verifyComplete();
    }

    @Test
    void testExtractEventIds() {
        MatchEvent.Actor primary = new MatchEvent.Actor("pId1", Event.Actor.Type.PLAYER, "tId1");
        MatchEvent.Actor secondary = new MatchEvent.Actor("pId2", Event.Actor.Type.UNKNOWN, "tId2");
        MatchStateData.MatchEventWrapper evtWrapper = createdEvent("eventId", eventWithActors(primary, secondary));
        MatchStateData data = new MatchStateData(List.of(evtWrapper), update(dummyMatchState()),
                update(dummyMatchInfo()), null, null);
        Map<String, Team> expectedTeams = extractorReturns("tId1", "tId2");

        Mono<Map<String, Team>> result = teamExtractor.extract(data);

        verify(teamClient).getByTeamIds(Set.of("tId1", "tId2"));
        StepVerifier.create(result).expectNext(expectedTeams).verifyComplete();
    }

    @Test
    void testExtractMatchInfoIds() {
        MatchInfo matchInfo = dummyMatchInfo("1", "2");
        MatchStateData data = new MatchStateData(null, update(dummyMatchState()), update(matchInfo), null, null);
        Map<String, Team> expectedTeams = extractorReturns("1", "2");

        Mono<Map<String, Team>> result = teamExtractor.extract(data);

        verify(teamClient).getByTeamIds(Set.of("1", "2"));
        StepVerifier.create(result).expectNext(expectedTeams).verifyComplete();
    }

    @Test
    void testExtractLineupIds() {
        Lineup.Team homeTeam = lineupTeam("1");
        Lineup.Team awayTeam = lineupTeam("2");
        Lineup lineup = new Lineup("mId", homeTeam, awayTeam, null);
        MatchStateData data = new MatchStateData(null, update(dummyMatchState()),
                update(dummyMatchInfo()), updateLineup(lineup), null);
        Map<String, Team> expectedTeams = extractorReturns("1", "2");

        Mono<Map<String, Team>> result = teamExtractor.extract(data);

        verify(teamClient).getByTeamIds(Set.of("1", "2"));
        StepVerifier.create(result).expectNext(expectedTeams).verifyComplete();
    }

    private Map<String, Team> extractorReturns(String... ids) {
        List<Team> teams = Stream.of(ids)
                .map(CompetitionTestModels::dummyTeam).collect(Collectors.toList());
        Map<String, Team> teamIds = teams.stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));

        Mockito.doReturn(Flux.fromIterable(teams)).when(teamClient).getByTeamIds(Set.of(ids));

        return teamIds;
    }

    private Lineup.Team lineupTeam(String id) {
        return new Lineup.Team(id, List.of(), List.of(), List.of(), null, null);
    }

}
