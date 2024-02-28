package com.uefa.platform.service.b2bpush.core.domain.archive.service;

import com.uefa.platform.data.OffsetLimitRequest;
import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.model.ArchiveInputData;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.CriteriaBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.web.controller.param.Limit;
import com.uefa.platform.web.controller.param.Offset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class MessageArchiveServiceTests {

    @Mock
    MessageArchiveRepository messageArchiveRepository;

    MessageArchiveService messageArchiveService;

    Criteria criteria;
    Pageable page;

    List<MessageArchiveEntity> archiveEntities = List.of(
            new MessageArchiveEntity("id-1", Instant.now(),
            Instant.now().minus(1, ChronoUnit.DAYS),
            MessageProvider.MATCH_STATE_PROVIDER, "received",
            "{ text: the text to search for }",
            Status.SUCCESS, null,
            Map.of("matchId", "3000", "hasLineup", true,
                    "eventIds", Set.of("123", "321")), null, "feedName", null)
    );

    @BeforeEach
    void setup() {
        messageArchiveService = new MessageArchiveService(messageArchiveRepository);

        criteria = new CriteriaBuilder("3000")
                .withStatus(Status.SUCCESS)
                .withStartDate(Instant.now().minus(2, ChronoUnit.DAYS))
                .withEndDate(Instant.now().plus(3, ChronoUnit.DAYS))
                .withHasLineup(true)
                .build();

        page = OffsetLimitRequest.of(0, 10);
    }

    @Test
    void testGetArchives() {
        when(messageArchiveRepository.getArchives(any(Criteria.class), anyString(), any(Pageable.class)))
                .thenReturn(archiveEntities);

        ArchiveInputData inputData = new ArchiveInputData.Builder()
                .withMatchId("3000")
                .withProvider(MessageProvider.MATCH_STATE_PROVIDER)
                .withStatus(Status.SUCCESS)
                .withHasLineup(true)
                .withEventIds(null)
                .withStartDate(Instant.now().minus(2, ChronoUnit.DAYS))
                .withEndDate(Instant.now().plus(3, ChronoUnit.DAYS))
                .withText("search")
                .withOffset(Offset.of("0"))
                .withLimit(Limit.of("10"))
                .build();

        List<Archive> test = messageArchiveService.getArchiveMessages(inputData);

        Assertions.assertEquals(archiveEntities.size(), test.size());

        for (int i = 0; i < test.size(); i++) {
            Archive archive = test.get(i);
            MessageArchiveEntity entity = archiveEntities.get(i);

            Assertions.assertEquals(entity.getId(), archive.getId());
            Assertions.assertEquals(entity.getSentContent(), archive.getSentContent());
            Assertions.assertEquals(entity.getSentTimestamp(), archive.getSentTimestamp());
            Assertions.assertEquals(entity.getTags(), archive.getTags());
            Assertions.assertEquals(Archive.Provider.valueOf(entity.getProvider().name()),
                    archive.getProvider());
            Assertions.assertEquals(Archive.Status.valueOf(entity.getStatus().name()),
                    archive.getStatus());

            // Test order of converted List, by 'sentTimestamp'
            if (i < test.size() - 1) {
                Archive nextArchive = test.get(i + 1);
                Assertions.assertTrue(archive.getSentTimestamp().isAfter(nextArchive.getSentTimestamp()));
            }
        }
    }

}
