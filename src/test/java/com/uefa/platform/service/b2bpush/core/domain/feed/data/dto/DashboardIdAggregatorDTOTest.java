package com.uefa.platform.service.b2bpush.core.domain.feed.data.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class DashboardIdAggregatorDTOTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(DashboardIdAggregatorDTO.class).usingGetClass().verify();
    }
}
