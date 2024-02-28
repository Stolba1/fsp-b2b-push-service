package com.uefa.platform.service.b2bpush.core.domain.archive.repository;

import com.uefa.platform.data.OffsetLimitRequest;
import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.CriteriaBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.archive.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

// For testing Custom Repository methods
class MessageArchiveRepositoryImplTests extends AbstractIntegrationTest {

    @Autowired
    MessageArchiveRepository messageArchiveRepository;

    List<MessageArchiveEntity> messageArchives = TestUtils.messageArchives();

    Pageable page;

    @BeforeEach
    void setup() {
        messageArchiveRepository.saveAll(messageArchives);

        page = OffsetLimitRequest.of(0, 10);
    }

    @AfterEach
    void teardown() {
        messageArchiveRepository.deleteAll();
    }

    @Test
    void testQueryNonExistingMessage() {
        final String nonExistingMatchId = "12345677";

        Criteria criteria = new CriteriaBuilder(nonExistingMatchId).build();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(criteria, null, page);

        Assertions.assertEquals(0, test.size());
    }

    @Test
    void testQueryByMatchId() {
        Criteria criteria = new CriteriaBuilder("3000").build();

        final int expectedSize = (int) messageArchives.stream()
                .filter(archive ->
                        "3000".equals(String.valueOf(archive.getTags().get("matchId"))))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(criteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByProvider() {
        MessageProvider provider = MessageProvider.MATCH_STATE_PROVIDER;

        Criteria providerCriteria = new CriteriaBuilder("3000").withProvider(provider).build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getProvider() == provider)
                .count();

        List<MessageArchiveEntity> test = messageArchiveRepository.getArchives(providerCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByStatus() {
        MessageArchiveEntity.Status statusSuccess = Status.SUCCESS;

        Criteria statusCriteria = new CriteriaBuilder("3000").withStatus(statusSuccess).build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getStatus() == statusSuccess)
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(statusCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testHasLineupTrue() {
        Criteria hasLineupCriteria = new CriteriaBuilder("3000")
                .withHasLineup(true)
                .build();

        final int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .map(archive -> archive.getTags().get("hasLineup"))
                .filter(lineupExists -> (boolean) lineupExists)
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(hasLineupCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testHasLineupFalse() {
        final boolean hasLineup = false;

        Criteria doesNotHaveLineupCriteria = new CriteriaBuilder("3000").withHasLineup(hasLineup).build();

        final int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .map(archive -> archive.getTags().get("hasLineup"))
                .filter(value -> !(boolean) value)
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(doesNotHaveLineupCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByEventIds() {
        String eventId1 = "event-123";
        String eventId2 = "event-678";
        Set<String> eventIds = Set.of(eventId1, eventId2);

        Criteria eventsCriteria = new CriteriaBuilder("3000").withEventIds(eventIds).build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .map(archive -> archive.getTags().get("eventIds"))
                .filter(o -> o instanceof Set<?>)
                .filter(set -> ((Set<?>) set).contains(eventId1) ||
                        ((Set<?>) set).contains(eventId2))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(eventsCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByStartDate() {
        final Instant startDate = Instant.now();

        Criteria startDateCriteria = new CriteriaBuilder("3000").withStartDate(startDate).build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getSentTimestamp().isAfter(startDate))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(startDateCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByEndDate() {
        final Instant endDate = Instant.now().plus(2, ChronoUnit.DAYS);

        Criteria endDateCriteria = new CriteriaBuilder("3000").withEndDate(endDate).build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getSentTimestamp().isBefore(endDate))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(endDateCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByStartAndEndDate() {
        final Instant startDate = Instant.now();
        final Instant endDate = Instant.now().plus(3, ChronoUnit.DAYS);

        Criteria dateCriteria = new CriteriaBuilder("3000")
                .withStartDate(startDate)
                .withEndDate(endDate)
                .build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getSentTimestamp().isAfter(startDate) &&
                        archive.getSentTimestamp().isBefore(endDate))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(dateCriteria, null, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testQueryByText() {
        final String searchText = "search";
        final Criteria textCriteria = new CriteriaBuilder("3000").build();

        final int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(archive.getTags().get("matchId")))
                .filter(archive -> archive.getSentContent().contains(searchText))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(textCriteria, searchText, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

    @Test
    void testPaging() {
        page = OffsetLimitRequest.of(0, 2);

        Criteria dateCriteria = new CriteriaBuilder("3000")
                .withStartDate(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();

        List<MessageArchiveEntity> test = messageArchiveRepository.getArchives(dateCriteria, null, page);

        Assertions.assertEquals(2, test.size());
    }

    @Test
    void testSortingBySentTimestamp() {
        Criteria criteria = new CriteriaBuilder("3000").build();

        List<MessageArchiveEntity> test = messageArchiveRepository.getArchives(criteria, null, page);

        for (int i = 0; i < test.size() - 1; i++) {
            MessageArchiveEntity entity = test.get(i);
            MessageArchiveEntity nextEntity = test.get(i + 1);

            Assertions.assertTrue(entity.getSentTimestamp().isAfter(nextEntity.getSentTimestamp()));
        }
    }

    @Test
    void testAllCriteria() {
        Instant startDate = Instant.now();
        Instant endDate = Instant.now().plus(3, ChronoUnit.DAYS);
        String searchText = "search";

        Criteria allCriteria = new CriteriaBuilder("3000")
                .withProvider(MessageProvider.MATCH_STATISTICS_SERVICE)
                .withStatus(Status.SUCCESS)
                .withStartDate(startDate)
                .withEndDate(endDate)
                .withEventIds(Set.of("event-123"))
                .withHasLineup(true)
                .build();

        int expectedSize = (int) messageArchives.stream()
                .filter(archive -> "3000".equals(String.valueOf(archive.getTags().get("matchId"))) &&
                        archive.getSentTimestamp().isAfter(startDate) &&
                        archive.getSentTimestamp().isBefore(endDate) &&
                        (boolean) archive.getTags().get("hasLineup") &&
                        archive.getSentContent().contains("search") &&
                        archive.getStatus() == Status.SUCCESS &&
                        archive.getProvider() == MessageProvider.MATCH_STATISTICS_SERVICE)
                .map(archive -> archive.getTags().get("eventIds"))
                .filter(o -> o instanceof Set<?>)
                .filter(set -> ((Set<?>) set).contains("event-123"))
                .count();

        List<MessageArchiveEntity> test =
                messageArchiveRepository.getArchives(allCriteria, searchText, page);

        Assertions.assertEquals(expectedSize, test.size());
    }

}