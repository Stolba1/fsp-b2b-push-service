package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Hash;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HashRepository extends MongoRepository<Hash, Hash.HashIdentifier> {


}
