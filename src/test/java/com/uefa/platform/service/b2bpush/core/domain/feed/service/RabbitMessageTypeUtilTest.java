package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.RabbitContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RabbitMessageTypeUtilTest {

    @Test
    void testFormatMessageType() {
        Assertions.assertEquals("STATIC", RabbitMessageTypeUtil.formatMessageType(RabbitContentType.STATIC, null));
        Assertions.assertEquals("STATIC.FEED", RabbitMessageTypeUtil.formatMessageType(RabbitContentType.STATIC, "FEED"));
    }

}
