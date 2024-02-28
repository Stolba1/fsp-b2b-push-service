package com.uefa.platform.service.b2bpush.core.domain.feed.aggregator;

import com.uefa.platform.client.competition.v2.RoundClient;
import com.uefa.platform.dto.competition.v2.Round;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.AggregatorConstants.COMPETITION_ID_PARAM;
import static com.uefa.platform.service.b2bpush.core.domain.feed.aggregator.AggregatorConstants.SEASON_YEAR_PARAM;

@Component
public class RoundIdAggregator implements IdAggregator {

    private static final String NAME = "ROUND_ID_AGGREGATOR";

    private static final Logger LOGGER = LoggerFactory.getLogger(RoundIdAggregator.class);

    private final RoundClient roundClient;

    public RoundIdAggregator(RoundClient roundClient) {
        this.roundClient = roundClient;
    }

    @Override
    @NotNull
    public List<String> getRequiredParameters() {
        return List.of(COMPETITION_ID_PARAM, SEASON_YEAR_PARAM);
    }

    @Override
    @NotNull
    public Set<String> getIds(Map<String, String> parameters) {
        //check if list of parameters are correct
        final String seasonYearParam = parameters.get(SEASON_YEAR_PARAM);
        final String competitionIdParam = parameters.get(COMPETITION_ID_PARAM);

        if (getRequiredParameters().size() != parameters.size() ||
                !StringUtils.hasLength(seasonYearParam) ||
                !StringUtils.hasLength(competitionIdParam)) {
            LOGGER.error("Parameters do not match required for RoundIdAggregator provided {}, required {}!", parameters, getRequiredParameters());
            return new HashSet<>();
        }

        return Optional.ofNullable(roundClient.getRounds(competitionIdParam, seasonYearParam, null, null, null)
                        .doOnError(e -> LOGGER.error("Error extracting rounds for competition {} season {}", competitionIdParam, seasonYearParam, e))
                        .onErrorReturn(Collections.emptyList())
                        .block())
                .orElse(Collections.emptyList())
                .stream()
                .map(Round::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public String getIdAggregatorName() {
        return NAME;
    }

    @Override
    public boolean hasIdAggregatorName(String idAggregatorName) {
        return NAME.equals(idAggregatorName);
    }
}
