package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.RabbitContentType;
import org.springframework.util.StringUtils;

public class RabbitMessageTypeUtil {

    private RabbitMessageTypeUtil() {
        //hide from instantiation
    }

    public static String formatMessageType(RabbitContentType contentType, String feedName) {
        return StringUtils.hasLength(feedName) ? contentType.name() + "." + feedName : contentType.name();
    }

}
