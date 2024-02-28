package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.client.competition.v2.PlayerClient;
import com.uefa.platform.dto.competition.v2.Player;
import com.uefa.platform.dto.match.v2.Event;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
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

/**
 * Decorates the {@link B2bMatchStateData} object with player objects
 */
@Component
public class PlayerExtractor implements MatchStateExtractor<Map<String, Player>> {

    private final PlayerClient playerClient;

    public PlayerExtractor(PlayerClient playerClient) {
        this.playerClient = playerClient;
    }

    @Override
    public Mono<Map<String, Player>> extract(MatchStateData data) {
        Set<String> idSet = extractPlayerIds(data).collect(Collectors.toSet());

        return idSet.isEmpty() ? Mono.just(Map.of()) :
                playerClient.getByPlayerIds(idSet).collect(Collectors.toMap(Player::getId, Function.identity()));
    }

    private Stream<String> extractPlayerIds(MatchStateData matchStateData) {
        List<MatchStateData.MatchEventWrapper> eventUpdates = matchStateData.getEventUpdates();
        MatchStateData.LineupUpdate lineupUpdate = matchStateData.getLineupUpdate();

        // fetch players from the event primary/secondary actors
        Stream<String> eventActors = Optional.ofNullable(eventUpdates)
                .map(this::extractEventIds).orElse(Stream.empty());

        // lineup (field + bench) players
        Stream<String> lineupPlayers = Optional.ofNullable(lineupUpdate).map(this::extractLineupPlayers)
                .orElse(Stream.empty());

        return Stream.concat(eventActors, lineupPlayers);
    }

    private Stream<String> extractLineupPlayers(MatchStateData.LineupUpdate lineupUpdate) {
        Stream<String> homeIds = Optional.ofNullable(lineupUpdate.getLineup().getHomeTeam())
                .map(this::extractTeamPlayers).orElse(Stream.empty());
        Stream<String> awayIds = Optional.ofNullable(lineupUpdate.getLineup().getAwayTeam())
                .map(this::extractTeamPlayers).orElse(Stream.empty());

        return Stream.concat(homeIds, awayIds);
    }

    private Stream<String> extractTeamPlayers(Lineup.Team team) {
        var field = team.getField().stream().map(Lineup.Player::getPlayerId);
        var bench = team.getBench().stream().map(Lineup.Player::getPlayerId);

        return Stream.concat(field, bench);
    }

    private Stream<String> extractEventIds(@NotNull List<MatchStateData.MatchEventWrapper> eventUpdates) {
        return eventUpdates.stream().map(MatchStateData.MatchEventWrapper::getEvent)
                .filter(Objects::nonNull)
                .flatMap(this::extractPlayerIds);
    }

    private Stream<String> extractPlayerIds(@NotNull MatchEvent e) {
        return Stream.of(extractId(e.getPrimaryActor()), extractId(e.getSecondaryActor()))
                .flatMap(Optional::stream);
    }

    private Optional<String> extractId(MatchEvent.Actor actor) {
        return Optional.ofNullable(actor).filter(a -> a.getType() == Event.Actor.Type.PLAYER)
                .map(MatchEvent.Actor::getId);
    }

}
