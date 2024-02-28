package com.uefa.platform.service.b2bpush.core.domain.feed.aggregator;

import com.uefa.platform.client.competition.v2.TeamClient;
import com.uefa.platform.dto.competition.v2.TeamPlayer;
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
public class TeamIdAggregator implements IdAggregator {

    private static final String NAME = "TEAM_ID_AGGREGATOR";

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamIdAggregator.class);

    private final TeamClient teamClient;

    public TeamIdAggregator(TeamClient teamClient) {
        this.teamClient = teamClient;
    }

    @Override
    @NotNull
    public List<String> getRequiredParameters() {
        return List.of(COMPETITION_ID_PARAM, SEASON_YEAR_PARAM);
    }

    @Override
    @NotNull
    public Set<String> getIds(Map<String, String> parameters) {
        //check at least if list of parameters are correct
        final String competitionIdParam = parameters.get(COMPETITION_ID_PARAM);
        final String seasonYearParam = parameters.get(SEASON_YEAR_PARAM);

        if (getRequiredParameters().size() != parameters.size() ||
                !StringUtils.hasLength(competitionIdParam) ||
                !StringUtils.hasLength(seasonYearParam)) {
            LOGGER.error("Parameters do not match required for TeamIdAggregator provided {}, required {}!", parameters, getRequiredParameters());
            return new HashSet<>();
        }

        return Optional.ofNullable(teamClient.getTeamPlayers(competitionIdParam, seasonYearParam, null, null, null, null, null)
                        .doOnError(e -> LOGGER.error("Error extracting teamPlayers for competition {} season {}", competitionIdParam, seasonYearParam, e))
                        .onErrorReturn(Collections.emptyList())
                        .block())
                .orElse(Collections.emptyList())
                .stream()
                .map(TeamPlayer::getTeamId)
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
