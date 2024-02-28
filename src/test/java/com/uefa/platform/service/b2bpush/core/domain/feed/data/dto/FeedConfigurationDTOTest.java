package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class FeedConfigurationDTOTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(FeedConfigurationDTO.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", "456",
                List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        FeedConfigurationDTO configurationDTO = FeedConfigurationDTO.instanceOf(feedConfiguration);
        Assertions.assertEquals(feedConfiguration.getId(), configurationDTO.getId());
        Assertions.assertEquals(feedConfiguration.getFeedId(), configurationDTO.getFeedId());
        Assertions.assertEquals(feedConfiguration.getStatus(), configurationDTO.getStatus());
        Assertions.assertEquals(feedConfiguration.getStatus(), configurationDTO.getStatus());
        Assertions.assertEquals(feedConfiguration.getLastSentTime(), configurationDTO.getLastSentTime());
        Assertions.assertEquals(feedConfiguration.isPayloadSharedToClient(), configurationDTO.isPayloadSharedToClient());
        Assertions.assertEquals(feedConfiguration.getParameters().get(0).getValue(), configurationDTO.getParameters().get(0).getValue());
        Assertions.assertEquals(feedConfiguration.getParameters().get(0).getName(), configurationDTO.getParameters().get(0).getName());
    }
}

