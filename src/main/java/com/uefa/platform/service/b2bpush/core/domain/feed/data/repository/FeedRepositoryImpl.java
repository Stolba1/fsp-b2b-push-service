package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed.FIELD_LIVE_DATA_TYPE;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed.FIELD_STATUS;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.ACTIVE;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public FeedRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Feed upsert(Update update, String feedId) {
        return mongoTemplate.findAndModify(query(where("_id").is(feedId)), update,
                new FindAndModifyOptions().returnNew(true).upsert(true), Feed.class);
    }

    @Override
    public void changeStatus(String feedId, Status status) {
        mongoTemplate.updateMulti(query(where("_id").is(feedId)),
                new Update().set(FIELD_STATUS, status), Feed.class);
    }

    @Override
    public Optional<Feed> findActiveByLiveDataType(LiveFeedDataType liveFeedDataType) {
        List<Feed> results = mongoTemplate.find(new Query(where(FIELD_LIVE_DATA_TYPE).is(liveFeedDataType).and(FIELD_STATUS).is(ACTIVE)),
                Feed.class);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));

    }
}
