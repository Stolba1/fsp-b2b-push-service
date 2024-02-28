package com.uefa.platform.service.b2bpush.core.domain.archive.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageArchiveRepository extends MongoRepository<MessageArchiveEntity, String>,
        MessageArchiveRepositoryCustom {
}
