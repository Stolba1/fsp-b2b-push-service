package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.client.competition.v2.TeamClient;
import com.uefa.platform.dto.competition.v2.Team;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchInfo;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TeamExtractor implements MatchStateExtractor<Map<String, Team>> {

    private final TeamClient teamClient;

    public TeamExtractor(TeamClient teamClient) {
        this.teamClient = teamClient;
    }

    @Override
    public Mono<Map<String, Team>> extract(MatchStateData data) {
        Set<String> teamIds = extractTeamIds(data);
        return teamIds.isEmpty() ? Mono.just(Map.of()) :
                teamClient.getByTeamIds(teamIds).collect(Collectors.toMap(Team::getId, Function.identity()));
    }

    private Set<String> extractTeamIds(MatchStateData data) {

        // fetch teams from event actors
        Stream<String> actorTeams = Optional.ofNullable(data.getEventUpdates()).stream().flatMap(this::getActorTeamIds);

        // fetch teams from match info
        Stream<String> matchInfoTeams = getTeamIds(data.getMatchInfo().getData());

        // fetch teams from lineups
        Stream<String> lineupTeams = Optional.ofNullable(data.getLineupUpdate())
                .map(MatchStateData.LineupUpdate::getLineup).stream().flatMap(this::getLineupTeamIds);

        return Stream.of(actorTeams, matchInfoTeams, lineupTeams)
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
    }

    private Stream<String> getActorTeamIds(@NotNull List<MatchStateData.MatchEventWrapper> eventWrapper) {
        return eventWrapper.stream().map(MatchStateData.MatchEventWrapper::getEvent)
                .filter(Objects::nonNull)
                .flatMap(this::extractEventTeams);
    }

    private Stream<String> getTeamIds(@NotNull MatchInfo matchInfo) {
        return Stream.of(matchInfo.getHomeTeamId(), matchInfo.getAwayTeamId()).filter(Objects::nonNull);
    }

    private Stream<String> getLineupTeamIds(@NotNull Lineup lineup) {
        return Stream.of(extractId(lineup.getHomeTeam()), extractId(lineup.getAwayTeam()))
                .flatMap(Optional::stream);
    }

    private Stream<String> extractEventTeams(MatchEvent event) {
        return Stream.of(extractId(event.getPrimaryActor()), extractId(event.getSecondaryActor()))
                .flatMap(Optional::stream);
    }

    private Optional<String> extractId(MatchEvent.Actor actor) {
        return Optional.ofNullable(actor).map(MatchEvent.Actor::getTeamId);
    }

    private Optional<String> extractId(Lineup.Team team) {
        return Optional.ofNullable(team).map(Lineup.Team::getTeamId);
    }

}
