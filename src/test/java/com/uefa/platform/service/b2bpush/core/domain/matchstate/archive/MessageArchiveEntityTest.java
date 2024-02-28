package com.uefa.platform.service.b2bpush.core.domain.matchstate.archive;


import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class MessageArchiveEntityTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(MessageArchiveEntity.class).usingGetClass().verify();
    }

}
