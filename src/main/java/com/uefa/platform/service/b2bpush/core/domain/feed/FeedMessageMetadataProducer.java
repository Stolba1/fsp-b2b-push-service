package com.uefa.platform.service.b2bpush.core.domain.feed;

import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchInfoWrapper;
import com.uefa.platform.dto.message.matchstate.MatchStateData.LineupUpdate;
import com.uefa.platform.dto.message.matchstate.MatchStateData.MatchEventWrapper;
import com.uefa.platform.dto.message.matchstate.MatchStateData.OfficialsWrapper;
import com.uefa.platform.dto.message.matchstate.MatchStateWrapper;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FeedMessageMetadataProducer {

    public Map<String, String> createMetadata(Object data, String feedCode, List<Client.FeedConfiguration.Parameter> parameters,
                                              PlatformMessage.Type messageType) {
        var metadata = new HashMap<String, String>();
        metadata.put("content-type", "application/json");
        Optional.ofNullable(feedCode).ifPresent(code -> metadata.put("feed-name", feedCode));
        Optional.ofNullable(messageType).ifPresent(type -> metadata.put("type", messageType.toString()));
        parameters.forEach(param -> metadata.put("param-" + param.getName(), param.getValue()));
        metadata.put("publish-time", Instant.now().toString());
        if (data instanceof B2bMatchStateData matchStateMessage) {
            var matchStateData = matchStateMessage.getMatchStateData();
            Optional.ofNullable(matchStateData.getEventUpdates()).map(events -> events.size() > 1 ? events.get(0) : null).map(MatchEventWrapper::getAction)
                    .ifPresent(action -> metadata.put("data-eventUpdate-action", action.toString()));
            Optional.ofNullable(matchStateData.getLineupUpdate()).map(LineupUpdate::getAction)
                    .ifPresent(action -> metadata.put("data-lineupUpdate-action", action.toString()));
            Optional.ofNullable(matchStateData.getOfficialsUpdate()).map(OfficialsWrapper::getAction)
                    .ifPresent(action -> metadata.put("data-officialsUpdate-action", action.toString()));
            Optional.of(matchStateData.getMatchInfo()).map(MatchInfoWrapper::getAction)
                    .ifPresent(action -> metadata.put("data-matchInfo-action", action.toString()));
            Optional.of(matchStateData.getMatchState()).map(MatchStateWrapper::getAction)
                    .ifPresent(action -> metadata.put("data-matchState-action", action.toString()));
        }

        return metadata;
    }
}
