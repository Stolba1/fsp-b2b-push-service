package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice;
import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice.FeedWithCodeAlreadyExistsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.INACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardFeedServiceTest {

    @InjectMocks
    private DashboardFeedService dashboardFeedService;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ClientRepository clientRepository;

    @Test
    void testCreateFeed() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, null, 5, null, null, 30);
        Feed feedDb = Feed.instanceOf(feedDTO, null);

        when(feedRepository.existsByCode(any()))
                .thenReturn(false);
        when(feedRepository.save(any()))
                .thenReturn(feedDb);
        DashboardFeedDTO feed = dashboardFeedService.createFeed(feedDTO);
        Assertions.assertEquals(feedDTO, feed);
    }

    @Test
    void testCreateFeedAlreadyExists() {
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, null, 5, null, null, 30);
        Feed feedDb = Feed.instanceOf(feedDTO, null);

        when(feedRepository.existsByCode(any()))
                .thenReturn(true);

        FeedWithCodeAlreadyExistsException thrown = Assertions.assertThrows(
                FeedWithCodeAlreadyExistsException.class,
                () -> dashboardFeedService.createFeed(feedDTO)
        );
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("Feed with code: %s found in repository", feedDTO.getCode())));
    }

    @Test
    void testGetFeedById() {
        String feedId = "123";
        Feed feedDb = new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 0);

        when(feedRepository.findById(feedId))
                .thenReturn(Optional.of(feedDb));
        DashboardFeedDTO feedResponse = dashboardFeedService.getFeedById(feedId);
        Assertions.assertEquals(feedDb.getCode(), feedResponse.getCode());
    }

    @Test
    void testGetFeedByIdNotExists() {
        String feedId = "123";
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> dashboardFeedService.getFeedById(feedId));
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("No feed found for feedId:%s",
                        feedId)));
    }

    @Test
    void testGetFeeds() {
        String feedId = "123";
        Feed feedDb = new Feed(feedId, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);

        when(feedRepository.findAll())
                .thenReturn(List.of(feedDb));
        List<DashboardFeedDTO> feedsResponse = dashboardFeedService.getAllFeeds();
        Assertions.assertEquals(1, feedsResponse.size());
        Assertions.assertEquals(feedDb.getCode(), feedsResponse.get(0).getCode());
    }

    @Test
    void testGetEmptyListForFeeds() {
        when(feedRepository.findAll()).thenReturn(List.of());
        List<DashboardFeedDTO> feeds = dashboardFeedService.getAllFeeds();
        Assertions.assertEquals(0, feeds.size());
    }

    @Test
    void testUpdateFeed() {
        String feedId = "1";
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, null, 5, null, null, 30);
        Instant lastProcessingTime = Instant.now();
        Feed feed = Feed.instanceOf(feedDTO, lastProcessingTime);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedRepository.upsert(any(), eq(feedId))).thenReturn(feed);

        DashboardFeedDTO result = dashboardFeedService.updateFeed(feedId, feedDTO);
        Assertions.assertEquals(feedDTO.getCode(), result.getCode());
        Assertions.assertEquals(feedDTO.getStatus(), result.getStatus());
        Assertions.assertEquals(feedDTO.getParameters(), result.getParameters());
        Assertions.assertEquals(feedDTO.getUrl(), result.getUrl());
        Assertions.assertEquals(feedDTO.getProcessEveryMinutes(), result.getProcessEveryMinutes());
        Assertions.assertEquals(lastProcessingTime, result.getLastProcessingTime());
    }

    @Test
    void testUpdateFeedAlreadyExists() {
        String feedId = "456";
        String code = "code";
        DashboardFeedDTO feedDTO = new DashboardFeedDTO(null, "code", Status.ACTIVE,
                "url", Set.of("param1"), null, null, 5, null, null, null);

        when(feedRepository.existsByCodeAndIdNot(code, feedId))
                .thenReturn(true);

        ControllerAdvice.FeedWithCodeAlreadyExistsException thrown = Assertions.assertThrows(
                ControllerAdvice.FeedWithCodeAlreadyExistsException.class,
                () -> dashboardFeedService.updateFeed(feedId, feedDTO)
        );
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("Feed with code: %s found in repository", feedDTO.getCode())));
    }

    @Test
    void testChangeFeedStatus() {
        String feedId = "1";
        doNothing().when(feedRepository).changeStatus(feedId, INACTIVE);
        Feed existingFeed = new Feed("1", "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));

        dashboardFeedService.changeStatus(feedId, INACTIVE);
        verify(feedRepository).changeStatus(feedId, INACTIVE);
    }

    @Test
    void testDeleteById() {
        String feedId = "1";
        doNothing().when(feedRepository).deleteById(feedId);
        Feed existingFeed = new Feed("1", "code", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));
        doNothing().when(clientRepository).removeConfigurationsForFeedId(feedId);

        dashboardFeedService.deleteFeed(feedId);
        verify(feedRepository).deleteById(feedId);
    }

    @Test
    void testDeleteByIdWhenFeedNotExists() {
        String feedId = "1";
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> dashboardFeedService.deleteFeed(feedId));
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("No feed found for feedId:%s",
                        feedId)));
    }

}

