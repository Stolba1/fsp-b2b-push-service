package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientRepository extends MongoRepository<Client, String>, ClientRepositoryCustom {

    boolean existsByRoutingKey(String routingKey);

    boolean existsByRoutingKeyAndIdNot(String routingKey, String id);

    boolean existsByName(String clientName);

}
