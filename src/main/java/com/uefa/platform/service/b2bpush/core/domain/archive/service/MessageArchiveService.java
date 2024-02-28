package com.uefa.platform.service.b2bpush.core.domain.archive.service;

import com.uefa.platform.data.OffsetLimitRequest;
import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.model.ArchiveInputData;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.CriteriaBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.uefa.platform.service.b2bpush.core.domain.archive.converter.ArchiveConverter.convertArchiveEntities;

@Service
public class MessageArchiveService {

    private final MessageArchiveRepository messageArchiveRepository;

    @Autowired
    public MessageArchiveService(MessageArchiveRepository messageArchiveRepository) {
        this.messageArchiveRepository = messageArchiveRepository;
    }

    public List<Archive> getArchiveMessages(ArchiveInputData inputData) {
        Criteria criteria = new CriteriaBuilder(inputData.getMatchId())
                .withProvider(inputData.getProvider())
                .withStatus(inputData.getStatus())
                .withHasLineup(inputData.getHasLineup())
                .withEventIds(inputData.getEventIds())
                .withStartDate(inputData.getStartDate())
                .withEndDate(inputData.getEndDate())
                .build();

        int offset = inputData.getOffset().get();
        int limit = inputData.getLimit().get();
        Pageable page = OffsetLimitRequest.of(offset, limit);
        String text = inputData.getText();

        List<MessageArchiveEntity> archives = messageArchiveRepository.getArchives(criteria, text, page);

        return convertArchiveEntities(archives);
    }

}
