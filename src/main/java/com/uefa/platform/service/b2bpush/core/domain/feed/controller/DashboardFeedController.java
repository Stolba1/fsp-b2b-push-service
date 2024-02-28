package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.service.b2bpush.core.configuration.OpenApiConfiguration;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.exception.InvalidDtoFieldsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.DashboardFeedService;
import com.uefa.platform.web.exception.ArgumentCombinationNotValidException;
import com.uefa.platform.web.handler.DisableHttpCache;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardFeedController.DASHBOARD_URL_PREFIX;
import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardFeedController.URL_FEEDS;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO.LIVE_DATA_TYPE_PARAM;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO.PROCESS_EVERY_MINUTES_PARAM;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO.TYPE_PARAM;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardFeedDTO.URL_PARAM;


@RestController
@RequestMapping(path = DASHBOARD_URL_PREFIX + URL_FEEDS)
@Hidden
@DisableHttpCache
@Tag(name = OpenApiConfiguration.FSP_B2B_PUSH_SERVICE_TAG_STRING)
public class DashboardFeedController {
    public static final String DASHBOARD_URL_PREFIX = "/v1/dashboard";
    public static final String URL_FEEDS = "/feeds";
    private final DashboardFeedService feedService;

    public DashboardFeedController(DashboardFeedService feedService) {
        this.feedService = feedService;
    }

    @Operation(summary = "Returns feed by id")
    @GetMapping("/{feedId}")
    public DashboardFeedDTO getFeedById(@PathVariable(value = "feedId") String feedId) {
        return feedService.getFeedById(feedId);
    }

    @Operation(summary = "Returns all feeds")
    @GetMapping
    public List<DashboardFeedDTO> getAllFeeds() {
        return feedService.getAllFeeds();
    }

    @Operation(summary = "Create new feed")
    @PostMapping
    public DashboardFeedDTO createFeed(@Valid @RequestBody DashboardFeedDTO feedDTO,
                                       BindingResult bindingResult) {
        validateFeed(feedDTO, bindingResult);
        return feedService.createFeed(feedDTO);
    }

    @Operation(summary = "Update feed by id")
    @PutMapping("/{feedId}")
    public DashboardFeedDTO updateFeed(@PathVariable(value = "feedId") String feedId,
                                       @Valid @RequestBody DashboardFeedDTO feedDTO,
                                       BindingResult bindingResult) {
        validateFeed(feedDTO, bindingResult);
        return feedService.updateFeed(feedId, feedDTO);
    }

    @Operation(summary = "Change the feed status to provided value")
    @PatchMapping("/{feedId}")
    public void changeStatus(@PathVariable(value = "feedId") String feedId,
                             @RequestParam(value = "status") Status status) {
        feedService.changeStatus(feedId, status);
    }

    @Operation(summary = "Delete feed by id")
    @DeleteMapping("/{feedId}")
    public void deleteFeed(@PathVariable(value = "feedId") String feedId) {
        feedService.deleteFeed(feedId);
    }

    @Operation(summary = "Returns all possible values for live feed type")
    @GetMapping("/live/types")
    public List<LiveFeedDataType> getAllIdAggregators() {
        return Arrays.asList(LiveFeedDataType.values());
    }

    private void validateFeed(DashboardFeedDTO feedDTO, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            throw new InvalidDtoFieldsException(bindingResult);
        }

        if (feedDTO.getType().equals(FeedType.LIVE) && feedDTO.getLiveDataType() == null) {
            throw new ArgumentCombinationNotValidException(new LinkedHashSet<>(Arrays.asList(TYPE_PARAM, LIVE_DATA_TYPE_PARAM)),
                    Arrays.asList(feedDTO.getType(), feedDTO.getLiveDataType()));
        }

        if (feedDTO.getType().equals(FeedType.STATIC) && (feedDTO.getUrl() == null || feedDTO.getProcessEveryMinutes() == null)) {
            throw new ArgumentCombinationNotValidException(new LinkedHashSet<>(Arrays.asList(TYPE_PARAM, URL_PARAM, PROCESS_EVERY_MINUTES_PARAM)),
                    Arrays.asList(feedDTO.getType(), feedDTO.getUrl(), feedDTO.getProcessEveryMinutes()));
        }
    }
}
