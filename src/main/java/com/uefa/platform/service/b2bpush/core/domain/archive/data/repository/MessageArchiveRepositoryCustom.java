package com.uefa.platform.service.b2bpush.core.domain.archive.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface MessageArchiveRepositoryCustom {

    List<MessageArchiveEntity> getArchives(Criteria criteria, String text, Pageable page);

}
