package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.Parameter;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedConfigurationEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.IdAggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class ParameterValueResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterValueResolver.class);

    private static final String COMPETITION_ID_PARAM = "competitionId";
    private static final String SEASON_YEAR_PARAM = "seasonYear";

    private static final String ALL = "*";
    private static final String SEPARATOR = ";";
    private Map<String, List<String>> values;
    private final IdAggregatorService idAggregatorService;

    public ParameterValueResolver(ObjectMapper objectMapper, IdAggregatorService idAggregatorService) {
        this.idAggregatorService = idAggregatorService;
        try {
            final Reader fileReader = new InputStreamReader(new ClassPathResource("/parameters/supported-parameters.json")
                    .getInputStream(), StandardCharsets.UTF_8);
            values = objectMapper.readValue(new BufferedReader(fileReader), objectMapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, List.class));
        } catch (IOException ex) {
            LOG.error("Failed to load the parameters file", ex);
            values = Collections.emptyMap();
        }
    }

    /**
     * Check if the value is *  check if we have the value in the json file
     * and return the supported values
     * if multiple value then check if the key in the file and then do the split
     * if not return the value as is
     *
     * @param value the parameter value
     * @param name  the parameter name
     * @return the new value of the parameter
     */
    public List<String> getParameterValues(String value, String name) {
        if (!CollectionUtils.isEmpty(values) && values.get(name) != null) {
            if (ALL.equals(value)) {
                return values.get(name);
            } else if (value.contains(SEPARATOR)) {
                return Arrays.stream(value.split(SEPARATOR)).collect(Collectors.toList());
            }
        }
        return List.of(value);
    }

    public List<Map<String, String>> combineParametersValues(Map<String, List<String>> parameterMap) {
        return parameterMap.entrySet().stream()
                // collect list of elements as Map<String,String>
                .map(entry -> entry.getValue().stream()
                        .map(element -> Map.of(entry.getKey(), element))
                        .collect(Collectors.toList()))
                .reduce((list1, list2) -> list1.stream()
                        .flatMap(map1 -> list2.stream()
                                .map(map2 -> {
                                    // join entries of two maps
                                    Map<String, String> map = new LinkedHashMap<>();
                                    map.putAll(map1);
                                    map.putAll(map2);
                                    return map;
                                }))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    public Map<FeedConfigurationEntity, List<Client>> calculateUniqueFeedsWithClients(List<Client> activeClients, Map<String, Feed> activeFeeds) {
        Map<FeedConfigurationEntity, List<Client>> result = new HashMap<>();

        activeClients.forEach(client -> client.getConfigurations().stream()
                .filter(feedConfiguration -> Status.ACTIVE.equals(feedConfiguration.getStatus()))
                .forEach(feedConfiguration -> {
                    //we make sure the feed exists and is active
                    if (activeFeeds.containsKey(feedConfiguration.getFeedId())) {
                        //remove complex parameters if present, and get placeholder
                        final Map<String, List<String>> modifiedParameterMap = new HashMap<>();
                        feedConfiguration.getParameters().stream()
                                .filter(parameter -> parameter.getIdAggregatorName() == null)
                                .forEach(parameter -> modifiedParameterMap.put(parameter.getName(),
                                        this.getParameterValues(parameter.getValue(), parameter.getName())));

                        //check if it's a complex param and get all entity ids, so we can add them to the parameters
                        final List<Client.FeedConfiguration.Parameter> complexParameters = feedConfiguration.getParameters().stream()
                                .filter(parameter -> parameter.getIdAggregatorName() != null).toList();


                        final List<Map<String, String>> parameterList = getParameterList(complexParameters, modifiedParameterMap);

                        for (Map<String, String> params : parameterList) {

                            final FeedConfigurationEntity feedConfig = new FeedConfigurationEntity(feedConfiguration.getFeedId(),
                                    activeFeeds.get(feedConfiguration.getFeedId()).getUrl(),
                                    new TreeMap<>(params),
                                    activeFeeds.get(feedConfiguration.getFeedId()).getCode(),
                                    feedConfiguration.getId(),
                                    feedConfiguration.isPayloadSharedToClient());

                            if (result.containsKey(feedConfig)) {
                                List<Client> existingClient = result.get(feedConfig);
                                existingClient.add(client);
                            } else {
                                final List<Client> clients = new ArrayList<>();
                                clients.add(client);
                                result.put(feedConfig, clients);
                            }
                        }
                    }
                }));
        return result;
    }


    private List<Map<String, String>> getParameterList(List<Client.FeedConfiguration.Parameter> complexParameters,
                                                       Map<String, List<String>> modifiedParameterMap) {

        List<Map<String, String>> result = new ArrayList<>();
        if (complexParameters.size() == 1) {
            final Client.FeedConfiguration.Parameter complexParam = complexParameters.get(0);
            final Map<String, String> idAggregatorParams = complexParam.getIdAggregatorParameters();
            final Map<String, List<String>> modifiedIdAggregatorParameterMap = new HashMap<>();
            for (Map.Entry<String, String> param : idAggregatorParams.entrySet()) {
                modifiedIdAggregatorParameterMap.put(param.getKey(), this.getParameterValues(param.getValue(), param.getKey()));
            }
            final List<Map<String, String>> idAggregatorParameterList = this.combineParametersValues(modifiedIdAggregatorParameterMap);

            for (Map<String, String> paramList : idAggregatorParameterList) {
                final Client.FeedConfiguration.Parameter modifiedComplexParam = new Parameter(complexParam.getName(), complexParam.getValue()
                        , complexParam.getIdAggregatorName(), paramList);

                final Set<String> entityIds = idAggregatorService.getEntityIds(modifiedComplexParam);
                if (entityIds != null && !entityIds.isEmpty()) {
                    final Map<String, List<String>> filteredComplexParameters = new HashMap<>(modifiedParameterMap);
                    filteredComplexParameters.put(complexParam.getName(), new ArrayList<>(entityIds));
                    //i.e. we can't combine round ids for one competition with another.Need to make sure we have no such combinations
                    alignParameter(paramList, filteredComplexParameters, COMPETITION_ID_PARAM);
                    alignParameter(paramList, filteredComplexParameters, SEASON_YEAR_PARAM);

                    result.addAll(this.combineParametersValues(filteredComplexParameters));
                }
            }

        } else {
            //do all possible combinations of parameters for
            result.addAll(this.combineParametersValues(modifiedParameterMap));
        }

        return result;
    }

    private void alignParameter(Map<String, String> paramList, Map<String, List<String>> filteredComplexParameters, String parameterName) {
        if (filteredComplexParameters.containsKey(parameterName) && paramList.containsKey(parameterName)) {
            final String competitionId = paramList.get(parameterName);
            final List<String> competitionIdParams = new ArrayList<>();
            competitionIdParams.add(competitionId);
            filteredComplexParameters.put(parameterName, competitionIdParams);
        }
    }

}
