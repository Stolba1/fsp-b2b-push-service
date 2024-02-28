package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedConfigurationEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessStaticFeedResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) class ConfigurationProcessorServiceTest {

    @Mock
    private FeedHttpClient feedHttpClient;

    @Mock
    private FeedMessageProducer feedMessageProducer;

    @Mock
    private ConfigurationProcessorService service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new ConfigurationProcessorService(feedHttpClient, feedMessageProducer);
    }

    @Test
    void processStaticFeed() {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("competitionId", "1");
        paramsMap.put("seasonYear", "2023");

        FeedConfigurationEntity configEntity =
                new FeedConfigurationEntity("feedId", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}", paramsMap,
                        "code", "", true);

        when(feedHttpClient.getFeedResult(eq("https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2023"))).thenReturn("data");

        ProcessStaticFeedResult result = service.processStaticFeed(configEntity);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("data", result.getData());
        Assertions.assertEquals("https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2023", result.getFinalUrl());
        Assertions.assertEquals("8d777f385d3dfec8815d20f7496026dc", result.getHash());
        Assertions.assertEquals(paramsMap, result.getParameters());

    }

    @Test
    void processStaticFeedNullFinalUrl() {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("competitionId", "1");
        paramsMap.put("seasonYear", "2023");

        FeedConfigurationEntity configEntity =
                new FeedConfigurationEntity("feedId", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYearrr}", paramsMap,
                        "code", "", true);


        ProcessStaticFeedResult result = service.processStaticFeed(configEntity);

        Assertions.assertNull(result);
    }

    @Test
    void processStaticFeedNullFeedData() {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("competitionId", "1");
        paramsMap.put("seasonYear", "2023");

        FeedConfigurationEntity configEntity =
                new FeedConfigurationEntity("feedId", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}", paramsMap,
                        "code", "", true);

        when(feedHttpClient.getFeedResult(eq("https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2023"))).thenReturn(null);

        ProcessStaticFeedResult result = service.processStaticFeed(configEntity);

        Assertions.assertNull(result);
    }

}

