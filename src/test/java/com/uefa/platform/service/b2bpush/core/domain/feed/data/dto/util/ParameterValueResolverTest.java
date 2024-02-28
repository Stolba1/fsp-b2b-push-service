package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ParameterValueResolverTest {

    @Autowired
    private ParameterValueResolver parameterValueResolver;

    @Test
    void testCombineParametersValues() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("compId", List.of("1", "3", "17", "18", "2020", "14"));
        map.put("season", List.of("current"));
        map.put("stats", List.of("goal", "foul", "assist", "red_card"));
        List<Map<String, String>> result = parameterValueResolver.combineParametersValues(map);
        Assertions.assertEquals(24, result.size());
    }

    @Test
    void testGetParameterValues() {
        List<String> values = parameterValueResolver.getParameterValues("*", "competitionId");
        Assertions.assertNotNull(values);
        Assertions.assertEquals(3, values.size());
        Assertions.assertTrue(values.contains("2020"));
    }

    @Test
    void testGetParameterValuesMultipleValues() {
        List<String> values = parameterValueResolver.getParameterValues("17;18;14;3", "competitionId");
        Assertions.assertNotNull(values);
        Assertions.assertEquals(4, values.size());
        Assertions.assertTrue(values.contains("14"));
    }
}