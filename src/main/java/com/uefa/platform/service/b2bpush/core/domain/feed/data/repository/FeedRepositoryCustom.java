package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

public interface FeedRepositoryCustom {

    Feed upsert(Update update, String feedId);

    void changeStatus(String feedId, Status status);

    Optional<Feed> findActiveByLiveDataType(LiveFeedDataType liveFeedDataType);
}
