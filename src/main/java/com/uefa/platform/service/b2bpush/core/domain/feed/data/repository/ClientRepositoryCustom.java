package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public interface ClientRepositoryCustom {

    List<Client> findAllActiveClientsByConfigurationId(String configurationId);

    long updateHash(String clientId, String configurationId, String hash);

    long updateLastSentDate(String clientId, String configurationId);

    Client upsert(Update update, String clientId);

    void changeStatus(String clientId, Status status);

    void removeConfigurationsForFeedId(String feedId);
}
