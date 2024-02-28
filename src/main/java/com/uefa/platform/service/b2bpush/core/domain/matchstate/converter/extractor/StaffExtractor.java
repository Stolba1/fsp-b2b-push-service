package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.client.competition.v2.PersonClient;
import com.uefa.platform.dto.competition.v2.Person;
import com.uefa.platform.dto.match.v2.Event;
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

@Component
public class StaffExtractor implements MatchStateExtractor<Map<String, Person>> {

    private final PersonClient personClient;

    public StaffExtractor(PersonClient personClient) {
        this.personClient = personClient;
    }

    @Override
    public Mono<Map<String, Person>> extract(MatchStateData data) {
        Set<String> staffIds = extractStaffIds(data);

        return staffIds.isEmpty() ? Mono.just(Map.of()) :
                personClient.getByPersonIds(staffIds).collect(Collectors.toMap(Person::getId, Function.identity()));
    }

    private Set<String> extractStaffIds(MatchStateData data) {
        // extract from event
        Stream<String> eventIds = Optional.ofNullable(data.getEventUpdates())
                .map(this::extractEventIds).orElse(Stream.empty());
        // extract from lineup
        Stream<String> lineupIds = Optional.ofNullable(data.getLineupUpdate())
                .map(MatchStateData.LineupUpdate::getLineup).map(this::extractLineupIds).orElse(Stream.empty());

        return Stream.concat(eventIds, lineupIds).collect(Collectors.toSet());
    }

    private Stream<String> extractEventIds(@NotNull List<MatchStateData.MatchEventWrapper> eventUpdates) {
        return eventUpdates.stream().map(MatchStateData.MatchEventWrapper::getEvent)
                .filter(Objects::nonNull)
                .flatMap(this::extractStaffActorIds);
    }

    private Stream<String> extractStaffActorIds(@NotNull MatchEvent e) {
        return Stream.of(extractId(e.getPrimaryActor()), extractId(e.getSecondaryActor()))
                .flatMap(Optional::stream);
    }

    private Optional<String> extractId(MatchEvent.Actor actor) {
        return Optional.ofNullable(actor).filter(a -> a.getType() == Event.Actor.Type.COACH)
                .map(MatchEvent.Actor::getId);
    }

    private Stream<String> extractLineupIds(Lineup lineup) {
        Stream<String> homeTeamIds = Optional.ofNullable(lineup.getHomeTeam())
                .map(this::extractTeamIds).orElse(Stream.empty());
        Stream<String> awayTeamIds = Optional.ofNullable(lineup.getAwayTeam())
                .map(this::extractTeamIds).orElse(Stream.empty());

        return Stream.concat(homeTeamIds, awayTeamIds);
    }

    private Stream<String> extractTeamIds(Lineup.Team team) {
        return team.getStaff().stream().map(Lineup.Staff::getPersonId);
    }
}
