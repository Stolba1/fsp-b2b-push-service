package com.uefa.platform.service.b2bpush;

import com.uefa.platform.dto.competition.v2.CompetitionPhase;
import com.uefa.platform.dto.competition.v2.Round.Mode;
import com.uefa.platform.dto.competition.v2.Round.Type;
import com.uefa.platform.dto.match.v2.Event;
import com.uefa.platform.dto.match.v2.Match;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchInfo;
import com.uefa.platform.dto.message.matchstate.MatchInfoWrapper;
import com.uefa.platform.dto.message.matchstate.MatchOfficial;
import com.uefa.platform.dto.message.matchstate.MatchState;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateWrapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class MatchStateTestModels {

    private static final String MATCH_ID = "10";

    private MatchStateTestModels() {

    }

    public static MatchStateData dummyData() {
        return new MatchStateData(
                List.of(createdEvent("1", dummyGoal()), createdEvent("2", dummyGoal())),
                update(dummyMatchState()),
                update(dummyMatchInfo()),
                updateLineup(dummyLineup()),
                null

        );
    }

    public static MatchStateData dummyDataWithOfficials() {
        return new MatchStateData(
                List.of(createdEvent("1", dummyGoal()), createdEvent("2", dummyGoal())),
                update(dummyMatchState()),
                update(dummyMatchInfo()),
                updateLineup(dummyLineup()),
                updateMatchOfficials("1", Collections.singletonList(dummyMatchOfficial()))

        );
    }

    public static MatchInfo dummyMatchInfo() {
        return new MatchInfo("1", "1", "2020", "1", "1", "1", null,
                null, Instant.now(), null, null, null,
                null, Mode.KNOCK_OUT, CompetitionPhase.TOURNAMENT,
                null, null, null, Type.SEMIFINAL, null, null);
    }

    public static MatchInfo dummyMatchInfo(String homeTeamId, String awayTeamId) {
        return new MatchInfo("1", "1", "2020", "1", "1", "1", homeTeamId,
                awayTeamId, Instant.now(), null, null, null,
                null, Mode.KNOCK_OUT, CompetitionPhase.TOURNAMENT,
                null, null, null, Type.SEMIFINAL, null, null);
    }

    public static MatchInfoWrapper update(MatchInfo info) {
        return new MatchInfoWrapper(info, MatchInfoWrapper.Action.UPDATE);
    }

    public static MatchState dummyMatchState() {
        return new MatchState(MATCH_ID, Match.Status.LIVE, null, null, null, null, null, null, null, null, null);
    }

    public static MatchStateWrapper update(MatchState state) {
        return new MatchStateWrapper(state, MatchInfoWrapper.Action.UPDATE);
    }

    public static MatchEvent dummyGoal() {
        return new MatchEvent("eventId", "1", "2021", MATCH_ID, Event.Type.GOAL, null, null, Event.Phase.FIRST_HALF, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null);
    }

    public static MatchOfficial dummyMatchOfficial() {
        return new MatchOfficial("1000", MatchOfficial.Role.FOURTH_OFFICIAL);
    }

    public static MatchEvent eventWithActors(MatchEvent.Actor primary, MatchEvent.Actor secondary) {
        return new MatchEvent("eventId", "1", "2020", "1", Event.Type.GOAL, primary, secondary, Event.Phase.FIRST_HALF, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static MatchStateData.MatchEventWrapper createdEvent(String id, MatchEvent event) {
        return new MatchStateData.MatchEventWrapper(MatchStateData.MatchEventWrapper.Action.CREATE, id, event);
    }

    public static MatchStateData.LineupUpdate updateLineup(Lineup lineup) {
        return new MatchStateData.LineupUpdate(MatchStateData.LineupUpdate.Action.UPDATE, lineup);
    }

    public static MatchStateData.OfficialsWrapper updateMatchOfficials(String matchId, List<MatchOfficial> officials) {
        return new MatchStateData.OfficialsWrapper(MatchStateData.OfficialsWrapper.Action.UPDATE, matchId, officials);
    }

    public static Lineup dummyLineup() {
        return new Lineup(MATCH_ID, null, null, null);
    }

    public static Lineup.Player dummyPlayer(String id) {
        return new Lineup.Player(id, null, null, null, null, null, null, null, null);
    }

    public static Lineup.Staff dummyStaff(String id) {
        return new Lineup.Staff(id, Lineup.Staff.Role.COACH, null);
    }
}
