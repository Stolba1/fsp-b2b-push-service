package com.uefa.platform.service.b2bpush.core.domain.feed.data.converter;

import com.uefa.platform.dto.message.b2b.B2bFeedData;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;

import java.util.List;

public class B2bStaticFeedDataConverter {

    private B2bStaticFeedDataConverter() {
        //hide from instantiation
    }

    public static B2bFeedData convertPushDto(String data, String feedCode, List<Client.FeedConfiguration.Parameter> parameters) {
        List<B2bFeedData.Parameter> dtoParameters = parameters.stream()
                .map(param -> new B2bFeedData.Parameter(param.getName(), param.getValue()))
                .toList();

        return new B2bFeedData(new B2bFeedData.Info(feedCode, dtoParameters), data);
    }
}
