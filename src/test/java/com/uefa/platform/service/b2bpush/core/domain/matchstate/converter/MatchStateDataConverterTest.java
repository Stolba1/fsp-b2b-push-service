package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter;

import com.uefa.platform.dto.competition.v2.Person;
import com.uefa.platform.dto.competition.v2.Player;
import com.uefa.platform.dto.competition.v2.Team;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.PlayerExtractor;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.StaffExtractor;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.TeamExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.uefa.platform.service.b2bpush.CompetitionTestModels.dummyPerson;
import static com.uefa.platform.service.b2bpush.CompetitionTestModels.dummyPlayer;
import static com.uefa.platform.service.b2bpush.CompetitionTestModels.dummyTeam;
import static com.uefa.platform.service.b2bpush.MatchStateTestModels.dummyData;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchStateDataConverterTest {

    @Mock
    private PlayerExtractor playerExtractor;

    @Mock
    private TeamExtractor teamExtractor;

    @Mock
    private StaffExtractor staffExtractor;

    @InjectMocks
    private MatchStateDataConverter converter;

    private static final Map<String, Player> PLAYERS = Map.of("1", dummyPlayer("1"));
    private static final Map<String, Team> TEAMS = Map.of("1", dummyTeam("1"));
    private static final Map<String, Person> PERSONS = Map.of("1", dummyPerson("1"));

    @BeforeEach
    void init() {
        when(playerExtractor.extract(any())).thenReturn(Mono.just(PLAYERS));
        when(teamExtractor.extract(any())).thenReturn(Mono.just(TEAMS));
        when(staffExtractor.extract(any())).thenReturn(Mono.just(PERSONS));
    }

    @Test
    void testConvert() {
        MatchStateData originalData = dummyData();
        B2bMatchStateData converted = converter.convert(originalData);

        Assertions.assertEquals(new B2bMatchStateData(originalData, PLAYERS, TEAMS, PERSONS), converted);
    }

    @Test
    void testPlayerExtractorError() {
        MatchStateData originalData = dummyData();
        when(playerExtractor.extract(any())).thenReturn(Mono.error(new RuntimeException("Test Exception")));

        B2bMatchStateData converted = converter.convert(originalData);

        Assertions.assertEquals(new B2bMatchStateData(originalData, Map.of(), TEAMS, PERSONS), converted);
    }

    @Test
    void testTeamExtractorError() {
        MatchStateData originalData = dummyData();
        when(teamExtractor.extract(any())).thenReturn(Mono.error(new RuntimeException("Test Exception")));

        B2bMatchStateData converted = converter.convert(originalData);

        Assertions.assertEquals(new B2bMatchStateData(originalData, PLAYERS, Map.of(), PERSONS), converted);
    }

    @Test
    void testStaffExtractorError() {
        MatchStateData originalData = dummyData();
        when(staffExtractor.extract(any())).thenReturn(Mono.error(new RuntimeException("Test Exception")));

        B2bMatchStateData converted = converter.convert(originalData);

        Assertions.assertEquals(new B2bMatchStateData(originalData, PLAYERS, TEAMS, Map.of()), converted);
    }


}
