package com.uefa.platform.service.b2bpush.core.domain.feed;

import com.uefa.platform.dto.competition.v2.CompetitionPhase;
import com.uefa.platform.dto.match.v2.Event;
import com.uefa.platform.dto.match.v2.Event.Phase;
import com.uefa.platform.dto.match.v2.Match.Status;
import com.uefa.platform.dto.message.PlatformMessage.Type;
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
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.Parameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FeedMessageMetadataProducerTest {

    private final FeedMessageMetadataProducer feedMessageMetadataProducer = new FeedMessageMetadataProducer();

    private final List<Parameter> feedParameters = List.of(
            new Parameter("param1", "value1", null, null),
            new Parameter("param2", "value2", null, null)
    );

    @Test
    void noMetadata() {
        var result = feedMessageMetadataProducer.createMetadata(null, null, new ArrayList<>(), null);
        assertEquals("application/json", result.get("content-type"));
        assertNotNull(result.get("publish-time"));
    }

    @Test
    void staticFeedMetadata() {
        var result = feedMessageMetadataProducer.createMetadata("{}", "FEED_CODE", feedParameters, Type.B2B_STATIC_FEED);
        assertEquals("application/json", result.get("content-type"));
        assertEquals("FEED_CODE", result.get("feed-name"));
        assertEquals(Type.B2B_STATIC_FEED.toString(), result.get("type"));
        assertEquals("value1", result.get("param-param1"));
        assertEquals("value2", result.get("param-param2"));
        assertNotNull(result.get("publish-time"));
    }

    @Test
    void matchStateMetadataNoUpdatesStateOrInfo() {
        var matchStateData = new MatchStateData(null, getMatchStateWrapper(false), getMatchInfoWrapper(false), null, null);
        var result = feedMessageMetadataProducer.createMetadata(
                new B2bMatchStateData(matchStateData, null, null, null),
                "FEED_CODE",
                feedParameters,
                Type.B2B_LIVE_FEED);
        assertEquals("application/json", result.get("content-type"));
        assertEquals("FEED_CODE", result.get("feed-name"));
        assertEquals(Type.B2B_LIVE_FEED.toString(), result.get("type"));
        assertEquals("value1", result.get("param-param1"));
        assertEquals("value2", result.get("param-param2"));
        assertNotNull(result.get("publish-time"));
    }
    @Test
    void matchStateMetadataNoUpdatesOrInfoEmptyEventUpdateList() {
        var matchStateData = new MatchStateData(new ArrayList<>(), getMatchStateWrapper(false), getMatchInfoWrapper(false), null, null);
        var result = feedMessageMetadataProducer.createMetadata(
                new B2bMatchStateData(matchStateData, null, null, null),
                "FEED_CODE",
                feedParameters,
                Type.B2B_LIVE_FEED);
        assertEquals("application/json", result.get("content-type"));
        assertEquals("FEED_CODE", result.get("feed-name"));
        assertEquals(Type.B2B_LIVE_FEED.toString(), result.get("type"));
        assertEquals("value1", result.get("param-param1"));
        assertEquals("value2", result.get("param-param2"));
        assertNotNull(result.get("publish-time"));
    }

    @Test
    void matchStateMetadata() {
        var matchStateData =
                new MatchStateData(getMatchEvents(), getMatchStateWrapper(true), getMatchInfoWrapper(true), getLineupUpdate(), getOfficialsUpdate());
        var result = feedMessageMetadataProducer.createMetadata(
                new B2bMatchStateData(matchStateData, null, null, null),
                "FEED_CODE",
                feedParameters,
                Type.B2B_LIVE_FEED);
        assertEquals("application/json", result.get("content-type"));
        assertEquals("FEED_CODE", result.get("feed-name"));
        assertEquals(Type.B2B_LIVE_FEED.toString(), result.get("type"));
        assertEquals("value1", result.get("param-param1"));
        assertEquals("value2", result.get("param-param2"));
        assertEquals(MatchEventWrapper.Action.UPDATE.toString(), result.get("data-eventUpdate-action"));
        assertEquals(Action.CREATE.toString(), result.get("data-lineupUpdate-action"));
        assertEquals(OfficialsWrapper.Action.UPDATE.toString(), result.get("data-officialsUpdate-action"));
        assertEquals(MatchInfoWrapper.Action.NO_CHANGE.toString(), result.get("data-matchInfo-action"));
        assertEquals(MatchInfoWrapper.Action.DELETE.toString(), result.get("data-matchState-action"));
        assertNotNull(result.get("publish-time"));
    }

    private MatchStateWrapper getMatchStateWrapper(boolean withAction) {
        return new MatchStateWrapper(
                new MatchState("matchId", Status.LIVE, null, null,
                        null, null, null, null, null, null, null),
                withAction ? MatchInfoWrapper.Action.DELETE : null
        );
    }

    private MatchInfoWrapper getMatchInfoWrapper(boolean withAction) {
        return new MatchInfoWrapper(
                new MatchInfo("id", "compId", "2022", null, null, null, null,
                        null, Instant.now(), null, null, null, null,
                        null, CompetitionPhase.QUALIFYING, null, null, null, null,
                        null, null),
                withAction ? MatchInfoWrapper.Action.NO_CHANGE : null
        );
    }

    private List<MatchEventWrapper> getMatchEvents() {
        MatchEvent pass = new MatchEvent("eventId", "compId", "2022", "matchId", Event.Type.PASS, null, null,
                Phase.FIRST_HALF, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        MatchEvent goal = new MatchEvent("eventId", "compId", "2022", "matchId", Event.Type.GOAL, null, null,
                Phase.EXTRA_TIME_FIRST_HALF, null, null, null, null, null, null, null,
                null, null, null, null, null, null);

        return List.of(
                new MatchEventWrapper(
                        MatchEventWrapper.Action.UPDATE, "eventId",
                        pass
                ),
                new MatchEventWrapper(
                        MatchEventWrapper.Action.CREATE, "eventId",
                        pass
                )
        );
    }

    private LineupUpdate getLineupUpdate() {
        return new LineupUpdate(Action.CREATE, new Lineup("matchId", null, null, null));
    }

    private OfficialsWrapper getOfficialsUpdate() {
        return new OfficialsWrapper(OfficialsWrapper.Action.UPDATE, "matchId", null);
    }
}
