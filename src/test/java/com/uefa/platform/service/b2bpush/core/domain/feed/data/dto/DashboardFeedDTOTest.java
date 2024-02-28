package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

class DashboardFeedDTOTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DashboardFeedDTO.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        Feed feed = new Feed("123", "code", Status.ACTIVE,
                "url", Set.of("param1"), Set.of("matchId"), Instant.now(), 5, null, null, 30);
        DashboardFeedDTO feedDTOResponse = DashboardFeedDTO.instanceOf(feed);
        Assertions.assertEquals(feed.getId(), feedDTOResponse.getId());
        Assertions.assertEquals(feed.getCode(), feedDTOResponse.getCode());
        Assertions.assertEquals(feed.getStatus(), feedDTOResponse.getStatus());
        Assertions.assertEquals(feed.getUrl(), feedDTOResponse.getUrl());
        Assertions.assertEquals(feed.getParameters(), feedDTOResponse.getParameters());
        Assertions.assertEquals(feed.getBootstrapParameters(), feedDTOResponse.getBootstrapParameters());
        Assertions.assertEquals(feed.getLastProcessingTime(), feedDTOResponse.getLastProcessingTime());
        Assertions.assertEquals(feed.getProcessEveryMinutes(), feedDTOResponse.getProcessEveryMinutes());
    }
}

