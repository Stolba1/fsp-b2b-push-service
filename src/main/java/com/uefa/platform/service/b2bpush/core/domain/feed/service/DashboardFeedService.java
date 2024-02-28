package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice;
import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice.FeedWithCodeAlreadyExistsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardFeedService {
    private static final String FEED_NOT_FOUND_EXCEPTION = "No feed found for feedId:%s";
    private final FeedRepository feedRepository;
    private final ClientRepository clientRepository;

    public DashboardFeedService(FeedRepository feedRepository, ClientRepository clientRepository) {
        this.feedRepository = feedRepository;
        this.clientRepository = clientRepository;
    }

    public DashboardFeedDTO createFeed(DashboardFeedDTO feedDTO) {
        if (feedRepository.existsByCode(feedDTO.getCode())) {
            throw new FeedWithCodeAlreadyExistsException(feedDTO.getCode());
        }

        final Feed savedFeed = feedRepository.save(Feed.instanceOf(feedDTO, null));
        return DashboardFeedDTO.instanceOf(savedFeed);
    }

    public DashboardFeedDTO getFeedById(final String feedId) {
        final Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(FEED_NOT_FOUND_EXCEPTION, feedId)));
        return DashboardFeedDTO.instanceOf(feed);
    }

    public List<DashboardFeedDTO> getAllFeeds() {
        List<Feed> feeds = feedRepository.findAll();

        return feeds.stream()
                .map(DashboardFeedDTO::instanceOf)
                .sorted(Comparator.comparing(DashboardFeedDTO::getCode))
                .collect(Collectors.toList());
    }

    public DashboardFeedDTO updateFeed(String feedId, DashboardFeedDTO feedDTO) {
        if (feedRepository.existsByCodeAndIdNot(feedDTO.getCode(), feedId)) {
            throw new ControllerAdvice.FeedWithCodeAlreadyExistsException(feedDTO.getCode());
        }

        Feed feed = feedRepository
                .findById(feedId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(FEED_NOT_FOUND_EXCEPTION, feedId)));

        final Feed savedFeed = feedRepository.upsert(Feed.FeedUpdateBuilder
                .create()
                .with(Feed.instanceOf(feedDTO, feed.getLastProcessingTime()))
                .build(), feedId);

        return DashboardFeedDTO.instanceOf(savedFeed);
    }

    public void deleteFeed(String feedId) {
        if (feedRepository.findById(feedId).isEmpty()) {
            throw new ResourceNotFoundException(String.format(FEED_NOT_FOUND_EXCEPTION, feedId));
        }

        clientRepository.removeConfigurationsForFeedId(feedId);
        feedRepository.deleteById(feedId);
    }

    public void changeStatus(String feedId, Status status) {
        if (feedRepository.findById(feedId).isEmpty()) {
            throw new ResourceNotFoundException(String.format(FEED_NOT_FOUND_EXCEPTION, feedId));
        }

        feedRepository.changeStatus(feedId, status);
    }
}
