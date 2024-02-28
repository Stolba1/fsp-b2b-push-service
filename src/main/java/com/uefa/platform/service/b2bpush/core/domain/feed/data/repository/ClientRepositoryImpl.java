package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.mongodb.client.result.UpdateResult;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FIELD_CONFIGURATIONS;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FIELD_STATUS;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.FIELD_FEED_ID;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.FIELD_HASH;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.FIELD_ID;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client.FeedConfiguration.FIELD_LAST_SENT_TIME;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.ACTIVE;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class ClientRepositoryImpl implements ClientRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public ClientRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public long updateHash(String clientId, String configurationId, String hash) {
        final UpdateResult result = mongoTemplate.updateMulti(
                new Query(where("_id").is(clientId).and(FIELD_CONFIGURATIONS + "." + FIELD_ID).is(configurationId)),
                new Update().set(FIELD_CONFIGURATIONS + ".$." + FIELD_HASH, hash),
                Client.class
        );
        return result.getModifiedCount();
    }

    @Override
    public long updateLastSentDate(String clientId, String configurationId) {
        final UpdateResult result = mongoTemplate.updateMulti(
                new Query(where("_id").is(clientId).and(FIELD_CONFIGURATIONS + "." + FIELD_ID).is(configurationId)),
                new Update().set(FIELD_CONFIGURATIONS + ".$." + FIELD_LAST_SENT_TIME, Instant.now()),
                Client.class
        );
        return result.getModifiedCount();
    }


    @Override
    public Client upsert(Update update, String clientId) {
        return mongoTemplate.findAndModify(query(where("_id").is(clientId)), update,
                new FindAndModifyOptions().returnNew(true).upsert(true), Client.class);
    }

    @Override
    public void changeStatus(String clientId, Status status) {
        mongoTemplate.updateMulti(query(where("_id").is(clientId)),
                new Update().set(Client.FIELD_STATUS, status), Client.class);
    }

    @Override
    public void removeConfigurationsForFeedId(String feedId) {
        mongoTemplate.updateMulti(new Query(where(FIELD_CONFIGURATIONS + "." + FIELD_FEED_ID).is(feedId)),
                new Update().pull(FIELD_CONFIGURATIONS, new Query(where(FIELD_FEED_ID).is(feedId))), Client.class);
    }

    @Override
    public List<Client> findAllActiveClientsByConfigurationId(String configurationId) {
        return mongoTemplate.find(new Query(where(FIELD_CONFIGURATIONS + "." + FIELD_ID).is(configurationId).and(FIELD_STATUS).is(ACTIVE)),
                Client.class);
    }
}
