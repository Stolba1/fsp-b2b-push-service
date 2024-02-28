package com.uefa.platform.service.b2bpush.core.domain.archive.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Term;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class MessageArchiveRepositoryImpl implements MessageArchiveRepositoryCustom {

    private static final String KEY_SENT_TIMESTAMP = "sentTimestamp";
    private static final String ENGLISH_LANGUAGE = "en";

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MessageArchiveRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<MessageArchiveEntity> getArchives(Criteria searchCriteria,
                                                  String text,
                                                  Pageable page) {
        Query query = new Query(searchCriteria)
                .with(Sort.by(KEY_SENT_TIMESTAMP).descending())
                .with(page);

        // Additional criteria for text searches
        if (StringUtils.hasText(text)) {
            TextCriteria textCriteria = TextCriteria.forLanguage(ENGLISH_LANGUAGE)
                    .matching(new Term(text));
            query.addCriteria(textCriteria);
        }

        return mongoTemplate.find(query, MessageArchiveEntity.class);
    }

}
