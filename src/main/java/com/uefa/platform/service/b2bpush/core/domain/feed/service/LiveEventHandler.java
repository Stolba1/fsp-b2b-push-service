package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.dto.match.v2.Event.Type;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchEvent;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData.MatchEventWrapper;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class LiveEventHandler {

    /**
     * Processes {@link MatchStateMessage}s and filters out the {@link MatchEvent}s which are not included in the {@link EventPackage} configured for the
     * client. The other live messages remain intact.
     *
     * @param message the live message
     * @param client  the client
     * @return the adjusted {@link MatchStateMessage} or the initial message if no changes are made
     */
    public Object processMessage(Object message, Client client) {
        if (message instanceof B2bMatchStateData matchStateMessage) {
            var matchStateData = matchStateMessage.getMatchStateData();
            var eventsToSend = Optional.ofNullable(matchStateData.getEventUpdates())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(event -> canSendEvent(event, client))
                    .toList();

            final MatchStateData data = new MatchStateData(eventsToSend, matchStateData.getMatchState(), matchStateData.getMatchInfo(),
                    matchStateData.getLineupUpdate(), matchStateData.getOfficialsUpdate());
            return new B2bMatchStateData(data, null, null, null);
        }

        return message;
    }

    /**
     * Determines whether the current {@link MatchEvent} can be sent to the client.
     *
     * @param matchEvent the match event
     * @param client     the client
     * @return true if the match event can be sent to the client
     */
    private boolean canSendEvent(MatchEventWrapper matchEvent, Client client) {
        var eventType = matchEvent.getEvent().getType();
        var eventPackage = Optional.ofNullable(client.getEventPackage()).orElse(EventPackage.BASIC);

        boolean result = false;
        if (EventPackage.EXTENDED.equals(eventPackage)) {
            result = true;
        } else if (EventPackage.BASIC.equals(eventPackage)) {
            result = !Type.PASS.equals(eventType);
        }

        return result;
    }
}
