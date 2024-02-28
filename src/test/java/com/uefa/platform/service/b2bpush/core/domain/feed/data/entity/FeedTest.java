package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

class FeedTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Feed.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        DashboardFeedDTO dashboardFeedDTO = new DashboardFeedDTO("555", "code", Status.ACTIVE,
                "url", Set.of("param1", "param2"), Set.of("matchId"), Instant.now(), 5, null, null, 0);
        Instant lastProcessingTime = Instant.now();
        Feed feed = Feed.instanceOf(dashboardFeedDTO, lastProcessingTime);
        Assertions.assertEquals(dashboardFeedDTO.getCode(), feed.getCode());
        Assertions.assertEquals(dashboardFeedDTO.getStatus(), feed.getStatus());
        Assertions.assertEquals(dashboardFeedDTO.getUrl(), feed.getUrl());
        Assertions.assertEquals(dashboardFeedDTO.getProcessEveryMinutes(), feed.getProcessEveryMinutes());
        Assertions.assertEquals(dashboardFeedDTO.getParameters(), feed.getParameters());
        Assertions.assertEquals(dashboardFeedDTO.getBootstrapParameters(), feed.getBootstrapParameters());
        Assertions.assertEquals(lastProcessingTime, feed.getLastProcessingTime());
    }
}
