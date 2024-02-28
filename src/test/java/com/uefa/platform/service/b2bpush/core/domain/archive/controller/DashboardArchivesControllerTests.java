package com.uefa.platform.service.b2bpush.core.domain.archive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.archive.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class DashboardArchivesControllerTests extends AbstractIntegrationTest {

    static final String PATH = "/v1/dashboard/archives";
    static final String DEFAULT_OFFSET_LIMIT = "&offset=0&limit=10";

    final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    MessageArchiveRepository messageArchiveRepository;

    @Autowired
    ObjectMapper objectMapper;

    List<MessageArchiveEntity> messageArchives = TestUtils.messageArchives();

    @BeforeEach
    void setup() {
        teardown();
        messageArchiveRepository.saveAll(messageArchives);
    }

    @AfterEach
    void teardown() {
        messageArchiveRepository.deleteAll();
    }

    @DisplayName("Trying to query Archive endpoints from Instant dashboard")
    @Test
    void testQueryFromInstantDashboard() {
        final String path = PATH + "?matchId=3000" + DEFAULT_OFFSET_LIMIT;

        // Using 'get' method with Instant dashboard credentials
        get(path, withResponse(HttpStatus.FORBIDDEN));
    }

    @DisplayName("Cannot provide both EventIds and true HasLineup parametres")
    @Test
    void testBothEventsAndLineups() {
        final String path = PATH + "?matchId=3000&eventIds=123,321&hasLineup=true" + DEFAULT_OFFSET_LIMIT;

        getForDataExplorer(path, withResponse(HttpStatus.NOT_FOUND));
    }

    @DisplayName("Cannot query for Limit greater than 50")
    @Test
    void testQueryBigLimit() {
        final String path = PATH + "?matchId=3000&offset=0&limit=70";

        getForDataExplorer(path, withResponse(HttpStatus.BAD_REQUEST));
    }

    @DisplayName("StartDate cannot be after EndDate")
    @Test
    void testStartDateAfterEndDate() {
        Instant startDate = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant endDate = Instant.now().minus(5, ChronoUnit.HOURS);

        final String path = PATH +
                "?matchId=3000&startDateTime=" + formatter.format(startDate) +
                "&endDateTime=" + formatter.format(endDate) +
                DEFAULT_OFFSET_LIMIT;

        getForDataExplorer(path, withResponse(HttpStatus.NOT_FOUND));
    }

    @DisplayName("Querying by Match ID")
    @Test
    void testQueryByMatchId() throws Exception {
        final String matchId = "3000";
        final String matchPath = PATH + "?matchId=" + matchId + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> matchId.equals(message.getTags().get("matchId")))
                .count();

        List<Archive> test = getArchivesList(matchPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Querying by Provider")
    @Test
    void testQueryByProvider() throws Exception {
        final MessageProvider provider = MessageProvider.MATCH_STATE_PROVIDER;
        final String providerPath = PATH + "?matchId=3000&provider=" + provider.name() +
                DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> message.getProvider() == provider)
                .count();

        List<Archive> test = getArchivesList(providerPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Query by message Status")
    @Test
    void testQueryByStatus() throws Exception {
        final MessageArchiveEntity.Status status = Status.SUCCESS;
        final String statusPath = PATH + "?matchId=3000&status=SUCCESS" + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> status == message.getStatus())
                .count();

        List<Archive> test = getArchivesList(statusPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Query by Event IDS")
    @Test
    void testQueryByEventIds() throws Exception {
        final Set<String> eventIds = Set.of("event-123", "event-678");
        final String eventIdsStr = String.join(",", eventIds);
        final String eventsPath = PATH + "?matchId=3000&eventIds=" + eventIdsStr + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .map(message -> message.getTags().get("eventIds"))
                .filter(o -> o instanceof Set<?>)
                .filter(set -> ((Set<?>)set).contains("event-123") || ((Set<?>)set).contains("event-678"))
                .count();

        List<Archive> test = getArchivesList(eventsPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Query by HasLineup")
    @Test
    void testQueryHasLineup() throws Exception {
        final String lineupPath = PATH + "?matchId=3000&hasLineup=true" + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> (boolean) message.getTags().get("hasLineup"))
                .count();

        List<Archive> test = getArchivesList(lineupPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Querying by StartDateTime")
    @Test
    void testQueryByStartDateTime() throws Exception {
        Instant startTime = Instant.now();
        final String startDatePath = PATH + "?matchId=3000&startDateTime=" + formatter.format(startTime) +
                DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> message.getSentTimestamp().isAfter(startTime))
                .count();

        List<Archive> test = getArchivesList(startDatePath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Querying by EndDateTime")
    @Test
    void testQueryByEndDateTime() throws Exception {
        Instant endDateTime = Instant.now().plus(4, ChronoUnit.DAYS);
        final String endDatePath = PATH + "?matchId=3000&endDateTime=" + formatter.format(endDateTime) +
                DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> message.getSentTimestamp().isBefore(endDateTime))
                .count();

        List<Archive> test = getArchivesList(endDatePath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Querying by Start and End DateTimes")
    @Test
    void testQueryByStartAndEndDateTime() throws Exception {
        Instant startDateTime = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant endDateTime = Instant.now().plus(4, ChronoUnit.DAYS);
        final String datePath = PATH + "?matchId=3000&startDateTime=" + formatter.format(startDateTime) +
                "&endDateTime=" + formatter.format(endDateTime) +
                DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> message.getSentTimestamp().isAfter(startDateTime))
                .filter(message -> message.getSentTimestamp().isBefore(endDateTime))
                .count();

        List<Archive> test = getArchivesList(datePath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Querying by text")
    @Test
    void testQueryByText() throws Exception {
        final String searchTerm = "this is a field value";
        final String textPath = PATH + "?matchId=3000&text=" + searchTerm + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> message.getSentContent().contains(searchTerm))
                .count();

        List<Archive> test = getArchivesList(textPath);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Test offset and limit of queries")
    @Test
    void testOffsetAndLimit() throws Exception {
        final String path = PATH + "?matchId=3000&offset=0&limit=2";

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .limit(2)
                .count();

        List<Archive> test = getArchivesList(path);

        assertionsOfArchives(test, expectedSize);
    }

    @DisplayName("Query by all params")
    @Test
    void testQueryAllParams() throws Exception {
        final Instant startDate = Instant.now().minus(5, ChronoUnit.HOURS);
        final Instant endDate = Instant.now().plus(4, ChronoUnit.DAYS);
        final String searchText = "search";
        final MessageProvider provider = MessageProvider.MATCH_STATE_PROVIDER;

        final String allPath = PATH + "?matchId=3000&hasLineup=false&startDateTime=" + formatter.format(startDate) +
                "&endDateTime=" + formatter.format(endDate) + "&status=SUCCESS" + "&text=" + searchText +
                "&eventIds=event-123&provider=" + provider.name() + DEFAULT_OFFSET_LIMIT;

        int expectedSize = (int) messageArchives.stream()
                .filter(message -> "3000".equals(message.getTags().get("matchId")))
                .filter(message -> Status.SUCCESS == message.getStatus())
                .filter(message -> startDate.isAfter(message.getSentTimestamp()))
                .filter(message -> endDate.isBefore(message.getSentTimestamp()))
                .filter(message -> message.getSentContent().contains(searchText))
                .filter(message -> provider == message.getProvider())
                .filter(message -> !(boolean) message.getTags().get("hasLineup"))
                .map(message -> message.getTags().get("eventIds"))
                .filter(o -> o instanceof Set<?>)
                .filter(set -> ((Set<?>) set).contains("event-123"))
                .count();

        List<Archive> test = getArchivesList(allPath);

        assertionsOfArchives(test, expectedSize);
    }

    private void assertionsOfArchives(List<Archive> archives, int expectedSize) {
        Assertions.assertEquals(expectedSize, archives.size());
        assertOrderedArchives(archives);
    }

    private void assertOrderedArchives(List<Archive> archives) {
        for (int i = 0; i < archives.size() - 1; i++) {
            Archive archive = archives.get(i);
            Archive nextArchive = archives.get(i + 1);

            Assertions.assertTrue(archive.getSentTimestamp().isAfter(nextArchive.getSentTimestamp()));
        }
    }

    private List<Archive> getArchivesList(String path) throws Exception {
        String responseJson = getForDataExplorer(path, responseOk()).extract().asPrettyString();

        return Stream.of(objectMapper.readValue(responseJson, Archive[].class)).toList();
    }

}
