package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter;

import com.uefa.platform.dto.competition.v2.Person;
import com.uefa.platform.dto.competition.v2.Player;
import com.uefa.platform.dto.competition.v2.Team;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.PlayerExtractor;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.StaffExtractor;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor.TeamExtractor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.util.Map;

/**
 * Converts a {@link MatchStateData} instance coming from Match state provider to a
 * {@link B2bMatchStateData} which will be pushed by the B2B service
 */
@Component
public class MatchStateDataConverter implements Converter<MatchStateData, B2bMatchStateData> {

    private static final Logger LOG = LoggerFactory.getLogger(MatchStateDataConverter.class);

    private final PlayerExtractor playerExtractor;
    private final TeamExtractor teamExtractor;
    private final StaffExtractor staffExtractor;

    public MatchStateDataConverter(PlayerExtractor playerExtractor, TeamExtractor teamExtractor,
                                   StaffExtractor staffExtractor) {
        this.playerExtractor = playerExtractor;
        this.teamExtractor = teamExtractor;
        this.staffExtractor = staffExtractor;
    }

    @Override
    @NotNull
    public B2bMatchStateData convert(@NotNull MatchStateData source) {
        var matchId = source.getMatchInfo().getData().getId();

        // pull "enhanced" information for each entity type
        var players = playerExtractor.extract(source)
                .doOnError(e -> handleLoadingException(e, "players", matchId))
                .onErrorReturn(Map.of());
        var teams = teamExtractor.extract(source)
                .doOnError(e -> handleLoadingException(e, "teams", matchId))
                .onErrorReturn(Map.of());
        var staff = staffExtractor.extract(source)
                .doOnError(e -> handleLoadingException(e, "staff", matchId))
                .onErrorReturn(Map.of());

        // combine results
        return Mono.zip(Mono.just(source), players, teams, staff).map(this::combineResults)
                .doOnError(e -> LOG.error("Error encountered when enhancing entities", e))
                .blockOptional().orElse(new B2bMatchStateData(source, Map.of(), Map.of(), Map.of()));
    }

    private B2bMatchStateData combineResults(Tuple4<MatchStateData, Map<String, Player>,
            Map<String, Team>, Map<String, Person>> tuple) {
        return new B2bMatchStateData(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4());
    }

    private void handleLoadingException(Throwable e, String entity, String matchId) {
        LOG.warn("Problem loading {} for match {}:", entity, matchId, e);
    }

}
