package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParameterDTOTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ParameterDTO.class).usingGetClass().verify();
    }

    @Test
    void testInstanceOf() {
        Client.FeedConfiguration.Parameter parameter = new Client.FeedConfiguration.Parameter("name param", "value param", null, null);
        ParameterDTO parameterDTO = ParameterDTO.instanceOf(parameter);
        Assertions.assertEquals(parameter.getName(), parameterDTO.getName());
        Assertions.assertEquals(parameter.getValue(), parameterDTO.getValue());
    }
}
