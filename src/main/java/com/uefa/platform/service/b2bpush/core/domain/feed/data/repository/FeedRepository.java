package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.configuration.CacheConfiguration;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FeedRepository extends MongoRepository<Feed, String>, FeedRepositoryCustom {

    boolean existsByCode(String code);

    boolean existsById(@NotNull String feedId);

    Optional<Feed> findByIdAndStatus(String feedId, Status status);

    boolean existsByCodeAndIdNot(String code, String id);

    @Cacheable(CacheConfiguration.FEED_BY_LIVE_DATA_TYPE)
    Optional<Feed> findActiveByLiveDataType(LiveFeedDataType liveDataType);
}
