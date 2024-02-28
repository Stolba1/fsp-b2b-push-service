package com.uefa.platform.service.b2bpush.core.domain.archive.controller;

import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.service.b2bpush.core.configuration.OpenApiConfiguration;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.model.ArchiveInputData;
import com.uefa.platform.service.b2bpush.core.domain.archive.service.MessageArchiveService;
import com.uefa.platform.service.b2bpush.core.domain.exception.TooManyRequestedResultsException;
import com.uefa.platform.web.controller.param.Limit;
import com.uefa.platform.web.controller.param.Offset;
import com.uefa.platform.web.exception.ArgumentCombinationNotValidException;
import com.uefa.platform.web.handler.DisableHttpCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardFeedController.DASHBOARD_URL_PREFIX;

@RestController
@RequestMapping(path = DASHBOARD_URL_PREFIX + DashboardArchivesController.URL_ARCHIVES)
@DisableHttpCache
@Tag(name = OpenApiConfiguration.FSP_B2B_PUSH_SERVICE_TAG_STRING)
public class DashboardArchivesController {

    private static final int MAX_RESULTS = 50;

    public static final String URL_ARCHIVES = "/archives";

    private final MessageArchiveService messageArchiveService;

    @Autowired
    public DashboardArchivesController(MessageArchiveService messageArchiveService) {
        this.messageArchiveService = messageArchiveService;
    }

    @Operation(summary = "Returns Archive Messages based on input parameters")
    @GetMapping
    public List<Archive> getArchives(@Parameter(description = "Match ID to query archives for", style = ParameterStyle.FORM)
                                     @RequestParam(value = "matchId") String matchId,
                                     @Parameter(description = "Data provider of sent information",
                                             style = ParameterStyle.SIMPLE, explode = Explode.FALSE)
                                     @RequestParam(value = "provider", required = false) MessageProvider provider,
                                     @Parameter(description = "Status of sent messages",
                                             style = ParameterStyle.SIMPLE, explode = Explode.FALSE)
                                     @RequestParam(value = "status", required = false) Status status,
                                     @Parameter(description = "Event ID's of sent messages", style = ParameterStyle.FORM)
                                     @RequestParam(value = "eventIds", required = false) Set<String> eventIds,
                                     @Parameter(description = "Whether messages that include lineup should be searched for.",
                                             style = ParameterStyle.SIMPLE, explode = Explode.FALSE)
                                     @RequestParam(value = "hasLineup", required = false) Boolean hasLineup,
                                     @Parameter(description = "Instant/DateTime from which archives will be searched for. " +
                                             "Format is: YYYY-MM-DDTHH:mm:ss.000000000Z", style = ParameterStyle.FORM)
                                     @RequestParam(value = "startDateTime", required = false) Instant starDateTime,
                                     @Parameter(description = "Instant/DateTime until which archives will be searched for. " +
                                             "Format is: YYYY-MM-DDTHH:mm:ss.000000000Z", style = ParameterStyle.FORM)
                                     @RequestParam(value = "endDateTime", required = false) Instant endDateTime,
                                     @Parameter(description = "Text to be contained on searched archives' sent content.",
                                             style = ParameterStyle.FORM)
                                     @RequestParam(value = "text", required = false) String text,
                                     @Parameter(description = "Offset value - how many elements to skip from first matched archive.",
                                             style = ParameterStyle.FORM)
                                     @RequestParam(value = "offset") Offset offset,
                                     @Parameter(description = "Limit value - how many elements to query for, counting from the offset one.",
                                             style = ParameterStyle.FORM)
                                     @RequestParam(value = "limit") Limit limit) {
        validateParams(eventIds, hasLineup, starDateTime, endDateTime, limit);

        ArchiveInputData inputData = new ArchiveInputData.Builder()
                .withMatchId(matchId)
                .withProvider(provider)
                .withStatus(status)
                .withHasLineup(hasLineup)
                .withEventIds(eventIds)
                .withStartDate(starDateTime)
                .withEndDate(endDateTime)
                .withText(text)
                .withOffset(offset)
                .withLimit(limit)
                .build();

        return messageArchiveService.getArchiveMessages(inputData);
    }

    private void validateParams(Set<String> eventIds, Boolean hasLineup,
                                Instant starDateTime, Instant endDateTime,
                                Limit limit) {
        // Cannot query for both 'eventIds' and true 'hasLineup' in the same request
        if (!CollectionUtils.isEmpty(eventIds) && Boolean.TRUE.equals(hasLineup)) {
            throw new ArgumentCombinationNotValidException(
                    Set.of("eventIds", "hasLineup"),
                    Arrays.asList(eventIds, hasLineup)
            );
        }

        if (limit.get() > MAX_RESULTS) {
            throw new TooManyRequestedResultsException("limit", MAX_RESULTS);
        }

        // StartDateTime cannot be after EndDateTime
        if (Objects.nonNull(starDateTime) && Objects.nonNull(endDateTime) && starDateTime.isAfter(endDateTime)) {
            throw new ArgumentCombinationNotValidException(
                    Set.of("startDateTime", "endDateTime"),
                    Arrays.asList(starDateTime, endDateTime)
            );
        }

    }

}
