package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.dto.competition.v2.CompetitionPhase;
import com.uefa.platform.dto.match.v2.Event.Phase;
import com.uefa.platform.dto.match.v2.Event.Type;
import com.uefa.platform.dto.match.v2.Match.Status;
import com.uefa.platform.dto.message.LiveFeedMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bLiveFeedData;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.Lineup;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchInfo;
import com.uefa.platform.dto.message.matchstate.MatchInfoWrapper;
import com.uefa.platform.dto.message.matchstate.MatchState;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData.LineupUpdate;
import com.uefa.platform.dto.message.matchstate.MatchStateData.LineupUpdate.Action;
import com.uefa.platform.dto.message.matchstate.MatchStateData.MatchEventWrapper;
import com.uefa.platform.dto.message.matchstate.MatchStateData.OfficialsWrapper;
import com.uefa.platform.dto.message.matchstate.MatchStateWrapper;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LiveEventHandlerTest {

    private final LiveEventHandler liveEventHandler = new LiveEventHandler();

    @Mock
    private Client client;

    private final MatchStateWrapper matchStateWrapper = new MatchStateWrapper(
            new MatchState("matchId", Status.LIVE, null, null,
                    null, null, null, null, null, null, null),
            null
    );
    private final MatchInfoWrapper matchInfoWrapper = new MatchInfoWrapper(
            new MatchInfo("id", "compId", "2022", null, null, null, null,
                    null, Instant.now(), null, null, null, null,
                    null, CompetitionPhase.QUALIFYING, null, null, null, null,
                    null, null),
            null
    );
    private final LineupUpdate lineupUpdate = new LineupUpdate(Action.CREATE, new Lineup("matchId", null, null, null));
    private final OfficialsWrapper officialsWrapper = new OfficialsWrapper(OfficialsWrapper.Action.UPDATE, "matchId", null);
    private final MatchEvent pass = new MatchEvent("eventId", "compId", "2022", "matchId", Type.PASS, null, null,
            Phase.FIRST_HALF, null, null, null, null, null, null, null,
            null, null, null, null, null, null);
    private final MatchEvent goal = new MatchEvent("eventId", "compId", "2022", "matchId", Type.GOAL, null, null,
            Phase.EXTRA_TIME_FIRST_HALF, null, null, null, null, null, null, null,
            null, null, null, null, null, null);

    @Test
    public void testMessageNotMatchState() {
        var data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        var message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        var processedMessage = liveEventHandler.processMessage(message, client);

        assertEquals(message, processedMessage);
    }

    @Test
    public void testMessageNullEventUpdates() {
        var data = new MatchStateData(null, matchStateWrapper, matchInfoWrapper, lineupUpdate,
                officialsWrapper);
        var message = new B2bMatchStateData(data, null, null, null);
        var processedMessage = liveEventHandler.processMessage(message, client);
        var expectedMessage = new B2bMatchStateData(
                new MatchStateData(Collections.emptyList(), matchStateWrapper, matchInfoWrapper, lineupUpdate, officialsWrapper),
                null, null, null);
        assertEquals(expectedMessage, processedMessage);
    }

    @Test
    public void testMessageOnlyPassesClientHasBasicPackage() {
        Mockito.when(client.getEventPackage()).thenReturn(EventPackage.BASIC);
        var events = List.of(
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        goal
                )
        );
        var expectedEvents = List.of(new MatchEventWrapper(
                MatchEventWrapper.Action.UPDATE, "eventId",
                goal
        ));
        var data = new MatchStateData(events, matchStateWrapper, matchInfoWrapper, lineupUpdate,
                officialsWrapper);
        var message = new B2bMatchStateData(data, null, null, null);
        var processedMessage = liveEventHandler.processMessage(message, client);
        var expectedMessage = new B2bMatchStateData(
                new MatchStateData(expectedEvents, matchStateWrapper, matchInfoWrapper, lineupUpdate, officialsWrapper),
                null, null, null);

        assertEquals(expectedMessage, processedMessage);
    }

    @Test
    public void testMessageOnlyPassesClientNullPackage() {
        Mockito.when(client.getEventPackage()).thenReturn(null);
        var events = List.of(
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        goal
                )
        );
        var expectedEvents = List.of(new MatchEventWrapper(
                MatchEventWrapper.Action.UPDATE, "eventId",
                goal
        ));
        var data = new MatchStateData(events, matchStateWrapper, matchInfoWrapper, lineupUpdate,
                officialsWrapper);
        var message = new B2bMatchStateData(data, null, null, null);
        var processedMessage = liveEventHandler.processMessage(message, client);
        var expectedMessage = new B2bMatchStateData(
                new MatchStateData(expectedEvents, matchStateWrapper, matchInfoWrapper, lineupUpdate, officialsWrapper),
                null, null, null);

        assertEquals(expectedMessage, processedMessage);
    }


    @Test
    public void testMessageOnlyPassesClientHasExtendedPackage() {
        Mockito.when(client.getEventPackage()).thenReturn(EventPackage.EXTENDED);
        var events = List.of(
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        goal
                )
        );
        var data = new MatchStateData(events, matchStateWrapper, matchInfoWrapper, lineupUpdate,
                officialsWrapper);
        var message = new B2bMatchStateData(data, null, null, null);
        var processedMessage = liveEventHandler.processMessage(message, client);
        var expectedMessage = new B2bMatchStateData(
                new MatchStateData(events, matchStateWrapper, matchInfoWrapper, lineupUpdate, officialsWrapper),
                null, null, null);


        assertEquals(expectedMessage, processedMessage);
    }
}
