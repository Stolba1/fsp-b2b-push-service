package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class HashTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Hash.class).usingGetClass().verify();
    }

}
