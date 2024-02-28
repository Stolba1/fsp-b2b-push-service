package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.client.competition.v2.PersonClient;
import com.uefa.platform.dto.competition.v2.Person;
import com.uefa.platform.dto.match.v2.Event;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.CompetitionTestModels;
import com.uefa.platform.service.b2bpush.MatchStateTestModels;
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
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyMatchInfo;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyMatchState;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.eventWithActors;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.update;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.updateLineup;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaffExtractorTest {

    @Mock
    private PersonClient personClient;

    @InjectMocks
    private StaffExtractor staffExtractor;

    @Test
    void testExtractNoIds() {
        MatchStateData data = MatchStateTestModels.dummyData();

        Mono<Map<String, Person>> result = staffExtractor.extract(data);

        verify(personClient, never()).getByPersonIds(any());
        StepVerifier.create(result).expectNext(Map.of()).verifyComplete();
    }

    @Test
    void testExtractEventIds() {
        MatchEvent.Actor primary = new MatchEvent.Actor("pId1", Event.Actor.Type.COACH, "teamId");
        MatchEvent.Actor secondary = new MatchEvent.Actor("pId2", Event.Actor.Type.COACH, "teamId");
        MatchStateData.MatchEventWrapper evtWrapper = createdEvent("eventId", eventWithActors(primary, secondary));
        MatchStateData data = new MatchStateData(List.of(evtWrapper), update(dummyMatchState()),
                update(dummyMatchInfo()), null, null);
        Map<String, Person> expectedPersons = extractorReturns("pId1", "pId2");

        Mono<Map<String, Person>> result = staffExtractor.extract(data);

        verify(personClient).getByPersonIds(Set.of("pId1", "pId2"));
        StepVerifier.create(result)
                .expectNext(expectedPersons)
                .verifyComplete();
    }

    @Test
    void testExtractEventIds_IgnoresOtherActorTypes() {
        MatchEvent.Actor primary = new MatchEvent.Actor("pId1", Event.Actor.Type.PLAYER, "teamId");
        MatchEvent.Actor secondary = new MatchEvent.Actor("pId2", Event.Actor.Type.UNKNOWN, "teamId");
        MatchStateData.MatchEventWrapper evtWrapper = createdEvent("eventId", eventWithActors(primary, secondary));
        MatchStateData data = new MatchStateData(List.of(evtWrapper), update(dummyMatchState()),
                update(dummyMatchInfo()), null, null);


        Mono<Map<String, Person>> result = staffExtractor.extract(data);

        verify(personClient, never()).getByPersonIds(any());
        StepVerifier.create(result).expectNext(Map.of()).verifyComplete();
    }

    @Test
    void testExtractLineupIds() {
        Lineup.Team homeTeam = teamWithStaff(List.of("1", "2"));
        Lineup.Team awayTeam = teamWithStaff(List.of("3", "4"));
        Lineup lineup = new Lineup("mId", homeTeam, awayTeam, null);
        MatchStateData data = new MatchStateData(null, update(dummyMatchState()),
                update(dummyMatchInfo()), updateLineup(lineup), null);
        Map<String, Person> expectedPersons = extractorReturns("1", "2", "3", "4");

        Mono<Map<String, Person>> result = staffExtractor.extract(data);

        verify(personClient).getByPersonIds(Set.of("1", "2", "3", "4"));
        StepVerifier.create(result).expectNext(expectedPersons).verifyComplete();
    }

    private Map<String, Person> extractorReturns(String... ids) {
        List<Person> persons = Stream.of(ids)
                .map(CompetitionTestModels::dummyPerson).collect(Collectors.toList());
        Map<String, Person> personIds = persons.stream()
                .collect(Collectors.toMap(Person::getId, Function.identity()));

        Mockito.doReturn(Flux.fromIterable(persons)).when(personClient).getByPersonIds(Set.of(ids));

        return personIds;
    }

    private Lineup.Team teamWithStaff(List<String> staffIds) {
        List<Lineup.Staff> staff = staffIds.stream()
                .map(MatchStateTestModels::dummyStaff).collect(Collectors.toList());

        return new Lineup.Team("tId", List.of(), List.of(), staff, null, null);
    }
}
