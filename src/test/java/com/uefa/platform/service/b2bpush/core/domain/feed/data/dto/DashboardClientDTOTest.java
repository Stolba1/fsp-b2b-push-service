package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class DashboardClientDTOTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DashboardClientDTO.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", "456",
                List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Client client = new Client("123", "name b", "routing key",
                List.of(feedConfiguration),
                Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        DashboardClientDTO dashboardClientDTO = DashboardClientDTO.instanceOf(client);
        Assertions.assertEquals(client.getId(), dashboardClientDTO.getId());
        Assertions.assertEquals(client.getName(), dashboardClientDTO.getName());
        Assertions.assertEquals(client.getStatus(), dashboardClientDTO.getStatus());
        Assertions.assertEquals(client.getRoutingKey(), dashboardClientDTO.getRoutingKey());
        Assertions.assertTrue(dashboardClientDTO.getConfigurations().stream().findAny().isPresent());
        FeedConfigurationDTO feedConfigurationDTO = dashboardClientDTO.getConfigurations().stream().findAny().get();
        Assertions.assertEquals(client.getConfigurations().get(0).getId(), feedConfigurationDTO.getId());
        Assertions.assertEquals(client.getConfigurations().get(0).getFeedId(), feedConfigurationDTO.getFeedId());
        Assertions.assertEquals(client.getConfigurations().get(0).getStatus(), feedConfigurationDTO.getStatus());
        Assertions.assertEquals(client.getConfigurations().get(0).getLastSentTime(), feedConfigurationDTO.getLastSentTime());
        Assertions.assertEquals(client.getConfigurations().get(0).getParameters().get(0).getName(), feedConfigurationDTO.getParameters().get(0).getName());
        Assertions.assertEquals(client.getConfigurations().get(0).getParameters().get(0).getValue(), feedConfigurationDTO.getParameters().get(0).getValue());
        Assertions.assertEquals(client.getConfigurations().get(0).isPayloadSharedToClient(), feedConfigurationDTO.isPayloadSharedToClient());
        Assertions.assertEquals(client.getEventPackage(), dashboardClientDTO.getEventPackage());
    }

    @Test
    void testInstanceOfNullEventPackage() {
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", "456",
                List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Client client = new Client("123", "name b", "routing key",
                List.of(feedConfiguration),
                Status.ACTIVE, Instant.now(), null);
        DashboardClientDTO dashboardClientDTO = DashboardClientDTO.instanceOf(client);
        Assertions.assertEquals(client.getId(), dashboardClientDTO.getId());
        Assertions.assertEquals(client.getName(), dashboardClientDTO.getName());
        Assertions.assertEquals(client.getStatus(), dashboardClientDTO.getStatus());
        Assertions.assertEquals(client.getRoutingKey(), dashboardClientDTO.getRoutingKey());
        Assertions.assertTrue(dashboardClientDTO.getConfigurations().stream().findAny().isPresent());
        FeedConfigurationDTO feedConfigurationDTO = dashboardClientDTO.getConfigurations().stream().findAny().get();
        Assertions.assertEquals(client.getConfigurations().get(0).getId(), feedConfigurationDTO.getId());
        Assertions.assertEquals(client.getConfigurations().get(0).getFeedId(), feedConfigurationDTO.getFeedId());
        Assertions.assertEquals(client.getConfigurations().get(0).getStatus(), feedConfigurationDTO.getStatus());
        Assertions.assertEquals(client.getConfigurations().get(0).getLastSentTime(), feedConfigurationDTO.getLastSentTime());
        Assertions.assertEquals(client.getConfigurations().get(0).getParameters().get(0).getName(), feedConfigurationDTO.getParameters().get(0).getName());
        Assertions.assertEquals(client.getConfigurations().get(0).getParameters().get(0).getValue(), feedConfigurationDTO.getParameters().get(0).getValue());
        Assertions.assertEquals(EventPackage.BASIC, dashboardClientDTO.getEventPackage());
    }
}

