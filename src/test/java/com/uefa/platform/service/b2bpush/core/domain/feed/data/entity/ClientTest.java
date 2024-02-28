package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.FeedConfigurationDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.ParameterDTO;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class ClientTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Client.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        FeedConfigurationDTO feedConfiguration = new FeedConfigurationDTO("555", "456",
                List.of(new ParameterDTO("name param", "value param", null, null)),
                Status.ACTIVE, Instant.now(), true);
        DashboardClientDTO dashboardClientDTO = new DashboardClientDTO("123", "name b", "routing key",
                Status.ACTIVE, List.of(feedConfiguration), Instant.now(), EventPackage.BASIC);
        Client client = Client.instanceOf(dashboardClientDTO);
        Assertions.assertEquals(dashboardClientDTO.getName(), client.getName());
        Assertions.assertEquals(dashboardClientDTO.getStatus(), client.getStatus());
        Assertions.assertEquals(dashboardClientDTO.getRoutingKey(), client.getRoutingKey());
        Assertions.assertEquals(dashboardClientDTO.getConfigurations().get(0).getId(), client.getConfigurations().get(0).getId());
        Assertions.assertEquals(dashboardClientDTO.getConfigurations().get(0).getFeedId(), client.getConfigurations().get(0).getFeedId());
        Assertions.assertEquals(dashboardClientDTO.getConfigurations().get(0).getStatus(), client.getConfigurations().get(0).getStatus());
        Assertions.assertNull(client.getConfigurations().get(0).getLastSentTime());
        Assertions.assertNull(client.getConfigurations().get(0).getHash());
        Assertions.assertEquals(dashboardClientDTO.getConfigurations().get(0).getParameters().get(0).getName(),
                client.getConfigurations().get(0).getParameters().get(0).getName());
        Assertions.assertEquals(dashboardClientDTO.getConfigurations().get(0).getParameters().get(0).getValue(),
                client.getConfigurations().get(0).getParameters().get(0).getValue());
        Assertions.assertEquals(dashboardClientDTO.getEventPackage(), client.getEventPackage());
    }

    @Test
    void testFeedConfigurationInstanceOf() {
        FeedConfigurationDTO feedConfigurationDTO = new FeedConfigurationDTO("555", "456",
                List.of(new ParameterDTO("name param", "value param", null, null)),
                Status.ACTIVE, Instant.now(), true);
        Client.FeedConfiguration feedConfiguration = Client.FeedConfiguration.instanceOf(feedConfigurationDTO);
        Assertions.assertEquals(feedConfigurationDTO.getId(), feedConfiguration.getId());
        Assertions.assertEquals(feedConfigurationDTO.getFeedId(), feedConfiguration.getFeedId());
        Assertions.assertNull(feedConfiguration.getHash());
        Assertions.assertNull(feedConfiguration.getLastSentTime());
        Assertions.assertEquals(feedConfigurationDTO.getStatus(), feedConfiguration.getStatus());
        Assertions.assertEquals(feedConfigurationDTO.getParameters().get(0).getName(),
                feedConfiguration.getParameters().get(0).getName());
        Assertions.assertEquals(feedConfigurationDTO.getParameters().get(0).getValue(),
                feedConfiguration.getParameters().get(0).getValue());
    }

    @Test
    void testParametersInstanceOf() {
        ParameterDTO parameterDTO = new ParameterDTO("name param", "value param", null, null);
        Client.FeedConfiguration.Parameter parameter = Client.FeedConfiguration.Parameter.instanceOf(parameterDTO);
        Assertions.assertEquals(parameterDTO.getName(), parameter.getName());
        Assertions.assertEquals(parameterDTO.getValue(), parameter.getValue());
    }
}
