package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.common.ErrorResponse;
import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class DashboardFeedControllerTest extends AbstractIntegrationTest {
    private static final String PATH = "/v1/dashboard/feeds";

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        feedRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void testSaveFeed() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "TEAMS_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null,30);

        post(PATH, feedDTO, responseOk())
                .body("id", Matchers.notNullValue())
                .body("code", Matchers.is(feedDTO.getCode()))
                .body("status", Matchers.is(feedDTO.getStatus().name()))
                .body("url", Matchers.is(feedDTO.getUrl()))
                .body("parameters[0]", Matchers.is("param1"))
                .body("processEveryMinutes", Matchers.is(feedDTO.getProcessEveryMinutes()))
                .body("lastProcessingTime", Matchers.nullValue());
    }

    @Test
    void testSaveFeedLiveNoDataType() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "TEAMS_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.LIVE, null, 30);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();

        post(PATH, feedDTO, response)
                .body("error.message",
                        Matchers.is("Combination of values LIVE, null is not valid for type, liveDataType"));
    }

    @Test
    void testSaveFeedNoType() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "TEAMS_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.BAD_REQUEST.value())
                .build();

        post(PATH, feedDTO, response);
    }

    @Test
    void testSaveFeedStaticNoUrl() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "TEAMS_V2", Status.ACTIVE,
                null, Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();

        post(PATH, feedDTO, response)
                .body("error.message",
                        Matchers.is("Combination of values STATIC, null, 5 is not valid for type, url, processEveryMinutes"));
    }

    @Test
    void testFeedAlreadyExistsInDb() {
        String code = "TEAM_V2";
        Feed feed = new Feed("123", code, Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30);
        feedRepository.save(feed);

        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, code, Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        post(PATH, feedDTO, response)
                .body("error.message",
                        Matchers.is("Feed with code: TEAM_V2 found in repository"));

    }

    @Test
    void testUnauthorizedUser() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "TEAM_V1", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        postNoAuth(PATH, feedDTO, response);
    }

    @Test
    void testGetFeedById() {
        String feedId = "123";
        Instant lastProcessingTime = Instant.now();
        Feed feed1 = new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, lastProcessingTime, 5, null, null, 30);
        Feed feed2 = new Feed("567", "code1", Status.INACTIVE,
                "url1", Set.of("param2"), null, Instant.now(), 10, null, null, 30);
        feedRepository.saveAll(List.of(feed1, feed2));

        get(PATH + "/" + feedId, responseOk())
                .body("id", Matchers.is(feedId))
                .body("code", Matchers.is(feed1.getCode()))
                .body("status", Matchers.is(feed1.getStatus().name()))
                .body("url", Matchers.is(feed1.getUrl()))
                .body("parameters[0]", Matchers.is("param1"))
                .body("processEveryMinutes", Matchers.is(feed1.getProcessEveryMinutes()))
                .body("lastProcessingTime", Matchers.notNullValue());
    }

    @Test
    void testGetFeedByIdNotExists() {
        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        get(PATH + "/123", response);
    }

    @Test
    void testGetAllFeeds() {
        Feed feed1 = new Feed("123", "code1", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Feed feed2 = new Feed("567", "code", Status.INACTIVE,
                "url1", Set.of("param2"), null, Instant.now(), 10, null, null, 30);
        feedRepository.saveAll(List.of(feed1, feed2));

        get(PATH, responseOk())
                .body("[0].id", Matchers.is(feed2.getId()))
                .body("[0].code", Matchers.is(feed2.getCode()))
                .body("[0].status", Matchers.is(feed2.getStatus().name()))
                .body("[0].url", Matchers.is(feed2.getUrl()))
                .body("[0].parameters[0]", Matchers.is("param2"))
                .body("[0].processEveryMinutes", Matchers.is(feed2.getProcessEveryMinutes()))
                .body("[0].lastProcessingTime", Matchers.notNullValue())
                .body("[1].id", Matchers.is(feed1.getId()))
                .body("[1].code", Matchers.is(feed1.getCode()))
                .body("[1].status", Matchers.is(feed1.getStatus().name()))
                .body("[1].url", Matchers.is(feed1.getUrl()))
                .body("[1].parameters[0]", Matchers.is("param1"))
                .body("[1].processEveryMinutes", Matchers.is(feed1.getProcessEveryMinutes()))
                .body("[1].lastProcessingTime", Matchers.notNullValue());
    }

    @Test
    void testGetAllFeedsReturnsEmptyList() {
        get(PATH, responseOk())
                .body("size()", Matchers.is(0));
    }

    @Test
    void testUpdateFeed() {
        String feedId = "123";
        Feed existingFeed = feedRepository.save(new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30));

        DashboardFeedDTO dashboardFeedDTO = new DashboardFeedDTO(null, "TEAMS_V2", Status.INACTIVE,
                "url 123", Set.of("param2"), null, Instant.now(), 10, FeedType.STATIC, null, 30);
        put(PATH + "/" + feedId, dashboardFeedDTO, responseOk())
                .body("id", Matchers.notNullValue())
                .body("code", Matchers.is(dashboardFeedDTO.getCode()))
                .body("status", Matchers.is(dashboardFeedDTO.getStatus().name()))
                .body("url", Matchers.is(dashboardFeedDTO.getUrl()))
                .body("parameters[0]", Matchers.is("param2"))
                .body("processEveryMinutes", Matchers.is(dashboardFeedDTO.getProcessEveryMinutes()))
                .body("lastProcessingTime", Matchers.notNullValue());

        Optional<Feed> savedFeed = feedRepository.findById(feedId);
        Assertions.assertTrue(savedFeed.isPresent());
        Assertions.assertEquals(feedId, savedFeed.get().getId());
        assertSavedFeed(dashboardFeedDTO, savedFeed.get());
    }

    @Test
    void testUpdateFeedAlreadyExistsInDb() {
        String codeOld = "CODE";
        String codeNew = "CODE_2";
        String feedId = "123";
        Feed feedToBeUpdated = feedRepository.save(new Feed(feedId, codeOld, Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30));
        Feed feedWithSameCode = feedRepository.save(new Feed("456", codeNew, Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, FeedType.STATIC, null, 30));
        DashboardFeedDTO dashboardFeedDTO = new DashboardFeedDTO(null, codeNew, Status.INACTIVE,
                "url 123", Set.of("param2"), null, Instant.now(), 10, FeedType.STATIC, null, 30);

        feedRepository.saveAll(List.of(feedToBeUpdated, feedWithSameCode));

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        put(PATH + "/123", dashboardFeedDTO, response)
                .body("error.message",
                        Matchers.is("Feed with code: CODE_2 found in repository"));
    }

    @Test
    void testChangeFeedStatus() {
        String feedId = "123";
        Feed existingFeed = feedRepository.save(new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30));

        ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();
        patch(PATH + "/123?status=INACTIVE", response);

        Optional<Feed> savedFeed = feedRepository.findById(feedId);
        Assertions.assertTrue(savedFeed.isPresent());
        Assertions.assertEquals(existingFeed.getId(), savedFeed.get().getId());
        Assertions.assertEquals(existingFeed.getCode(), savedFeed.get().getCode());
        Assertions.assertEquals(existingFeed.getUrl(), savedFeed.get().getUrl());
        Assertions.assertEquals(existingFeed.getProcessEveryMinutes(), savedFeed.get().getProcessEveryMinutes());
        Assertions.assertEquals(existingFeed.getParameters(), savedFeed.get().getParameters());
        Assertions.assertEquals(Status.INACTIVE, savedFeed.get().getStatus());
    }

    @Test
    void testDeleteFeedById() {
        String feedId = "123";
        Feed existingFeed = new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Feed existingFeed2 = new Feed("888", "code2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        feedRepository.saveAll(List.of(existingFeed, existingFeed2));

        Client.FeedConfiguration existingFeedConfig1 = new Client.FeedConfiguration("777", feedId,
                List.of(new Client.FeedConfiguration.Parameter("name", "value", null, null)),
                "hash value", Instant.now(), Status.ACTIVE, true);
        Client.FeedConfiguration existingFeedConfig2 = new Client.FeedConfiguration("888", existingFeed2.getId(),
                List.of(new Client.FeedConfiguration.Parameter("name 2", "value 2", null, null)),
                "hash value 2", null, Status.ACTIVE, true);
        Client client = new Client("999", "name", "routing key",
                List.of(existingFeedConfig1, existingFeedConfig2),
                Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        clientRepository.save(client);


        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        delete(PATH + "/123", response);
        Optional<Feed> deletedFeed = feedRepository.findById(feedId);
        Assertions.assertFalse(deletedFeed.isPresent());
        Optional<Client> clientFromDb = clientRepository.findById(client.getId());
        Assertions.assertTrue(clientFromDb.isPresent());
        Assertions.assertEquals(1, clientFromDb.get().getConfigurations().size());
        Assertions.assertEquals(existingFeed2.getId(), clientFromDb.get().getConfigurations().get(0).getFeedId());
    }

    @Test
    void testDeleteFeedByIdNotExists() {
        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        delete(PATH + "/123", response)
                .body("error.message", Matchers.is("No feed found for feedId:123"));
    }

    @Test
    void testGetAggregators() {
        get(PATH + "/live/types", responseOk())
                .body("size()", Matchers.is(7))
                .body("[0]", Matchers.is("MATCH_PLAYER_STATISTICS"))
                .body("[1]", Matchers.is("MATCH_TEAM_STATISTICS"))
                .body("[2]", Matchers.is("COMPETITION_PLAYER_STATISTICS"))
                .body("[3]", Matchers.is("COMPETITION_TEAM_STATISTICS"))
                .body("[4]", Matchers.is("MATCH_STATE"))
                .body("[5]", Matchers.is("TRANSLATIONS"))
                .body("[6]", Matchers.is("PRE_MATCH"));
    }

    @Test
    void testCreateInvalidFeedDto() throws Exception {
        final String invalidCode = "code-567!!!/*";
        DashboardFeedDTO feedDto = new DashboardFeedDTO("id", invalidCode, Status.ACTIVE,
                "https://url.com/1", Collections.emptySet(), Collections.emptySet(),
                Instant.now(), 1, null, null, 30
        );

        String responseJson = post(PATH, feedDto, withResponse(HttpStatus.BAD_REQUEST)).extract().asPrettyString();

        assertErrorResponse(responseJson);
    }

    @Test
    void testUpdateInvalidFeedDto() throws Exception {
        final String invalidCode = "---+++--code";
        DashboardFeedDTO feedDto = new DashboardFeedDTO("id", invalidCode, Status.ACTIVE,
                "https://url.com/1", Collections.emptySet(), Collections.emptySet(),
                Instant.now(), 1, null, null, 30
        );

        String responseJson = patch(PATH + "/feed-123", feedDto,
                withResponse(HttpStatus.BAD_REQUEST))
                .extract().asPrettyString();

        assertErrorResponse(responseJson);
    }

    @DisplayName("Trying to query from Data Explorer")
    @Test
    void testQueryFromDataExplorer() {
        final String feedPath = PATH + "/feed-123";

        getForDataExplorer(feedPath, withResponse(HttpStatus.FORBIDDEN));
    }

    private void assertSavedFeed(DashboardFeedDTO dashboardFeedDTO, Feed savedFeed) {
        Assertions.assertEquals(dashboardFeedDTO.getCode(), savedFeed.getCode());
        Assertions.assertEquals(dashboardFeedDTO.getStatus(), savedFeed.getStatus());
        Assertions.assertEquals(dashboardFeedDTO.getUrl(), savedFeed.getUrl());
        Assertions.assertEquals(dashboardFeedDTO.getProcessEveryMinutes(), savedFeed.getProcessEveryMinutes());
        Assertions.assertEquals(dashboardFeedDTO.getParameters(), savedFeed.getParameters());
    }

    private void assertErrorResponse(String responseJson) throws Exception {
        ErrorResponse errorResponse = mapper.readValue(responseJson, ErrorResponse.class);

        Assertions.assertNotNull(errorResponse.getError().getMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getError().getStatus());
    }

}
