package com.uefa.platform.service.b2bpush.core.domain.feed.data.converter;

import com.uefa.platform.dto.message.b2b.B2bFeedData;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class B2bStaticFeedDataConverterTest {

    @Test
    void testConvertPushDto() {
        final String data = "{}";
        final String feedCode = "FEED_CODE";

        final Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        final Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);

        B2bFeedData result = B2bStaticFeedDataConverter.convertPushDto(data, feedCode, List.of(parameter1, parameter2));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(data, result.getData());
        Assertions.assertEquals(feedCode, result.getInfo().getFeedName());
        Assertions.assertEquals(2, result.getInfo().getParameters().size());
        Assertions.assertEquals("competitionId", result.getInfo().getParameters().get(0).getName());
        Assertions.assertEquals("1", result.getInfo().getParameters().get(0).getValue());
        Assertions.assertEquals("seasonYear", result.getInfo().getParameters().get(1).getName());
        Assertions.assertEquals("2022", result.getInfo().getParameters().get(1).getValue());
    }

}
