package com.uefa.platform.service.b2bpush.core.domain.archive.util;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestUtils {

    public static List<MessageArchiveEntity> messageArchives() {
        return List.of(
                new MessageArchiveEntity("id-1", Instant.now(),
                        Instant.now().minus(1, ChronoUnit.DAYS),
                        MessageProvider.MATCH_STATE_PROVIDER, "received",
                        "{ text: the text to search for }",
                        Status.SUCCESS, null,
                        Map.of("matchId", "3000", "hasLineup", true,
                                "eventIds", Collections.emptySet()), null, null, null),
                new MessageArchiveEntity("id-2", Instant.now(),
                        Instant.now().plus(5, ChronoUnit.DAYS),
                        MessageProvider.MATCH_STATISTICS_SERVICE, "received",
                        "{ text: and this is more text }",
                        Status.SUCCESS, null,
                        Map.of("matchId", "3000", "hasLineup", false,
                                "eventIds", Collections.emptySet()), null, "", null),
                new MessageArchiveEntity("id-3", Instant.now(),
                        Instant.now(),
                        MessageProvider.MATCH_STATISTICS_SERVICE, "received",
                        "{ text: field that has a search index }",
                        Status.ERROR, null,
                        Map.of("matchId", "4444", "hasLineup", true,
                                "eventIds", Set.of("event-678", "event-012")), null, null, null),
                new MessageArchiveEntity("id-4", Instant.now(),
                        Instant.now().plus(10, ChronoUnit.MINUTES),
                        MessageProvider.MATCH_STATE_PROVIDER, "received",
                        "{ text: search me }",
                        Status.ERROR, null,
                        Map.of("eventIds", Set.of("event-123", "event-321"),
                                "matchId", "3000", "hasLineup", true), null, null, null),
                new MessageArchiveEntity("id-5", Instant.now(),
                        Instant.now().plus(3, ChronoUnit.DAYS),
                        MessageProvider.MATCH_STATE_PROVIDER, "received",
                        "{ text: this is a field value }",
                        Status.ERROR, null,
                        Map.of("eventIds", Set.of("event-789", "event-123"),
                                "matchId", "56789", "hasLineup", false), null, null, null),
                new MessageArchiveEntity("id-6", Instant.now(),
                        Instant.now().plus(3, ChronoUnit.DAYS),
                        MessageProvider.COMPETITION_STATISTICS_SERVICE, "received",
                        "{ text: this is a field value }",
                        Status.ERROR, null,
                        Map.of("eventIds", Set.of("event-678", "event-78976"),
                                "matchId", "5000", "hasLineup", true), null, null, null)
        );
    }

}
