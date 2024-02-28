package com.uefa.platform.service.b2bpush.core.domain.archive.converter;

import com.uefa.platform.dto.common.archive.Archive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ArchiveConverterTests {

    static final String KEY_MATCH_ID = "matchId";

    List<MessageArchiveEntity> messageArchives = List.of(
            new MessageArchiveEntity("id-1", Instant.now(),
                    Instant.now().minus(1, ChronoUnit.DAYS),
                    MessageProvider.MATCH_STATE_PROVIDER, "received",
                    "{ text: the text to search for }",
                    Status.SUCCESS, null,
                    Map.of("matchId", "3000", "hasLineup", true,
                            "eventIds", Set.of("123", "321")), null, "feed1", FeedType.STATIC),
            new MessageArchiveEntity("id-2", Instant.now(),
                    Instant.now().plus(5, ChronoUnit.DAYS),
                    MessageProvider.MATCH_STATE_PROVIDER, "received",
                    "{ text: and this is more text }",
                    Status.SUCCESS, null,
                    Map.of("matchId", "5000", "hasLineup", false,
                            "eventIds", Collections.emptySet()), null, "feed2", FeedType.LIVE));

    @Test
    void testConvertToPlatformDtoList() {
        List<Archive> test = ArchiveConverter.convertArchiveEntities(messageArchives);

        Assertions.assertEquals(messageArchives.size(), test.size());

        for (int i = 0; i < test.size(); i++) {
            Archive archive = test.get(i);
            MessageArchiveEntity entity = messageArchives.get(i);

            Assertions.assertEquals(entity.getId(), archive.getId());
            Assertions.assertEquals(entity.getTags().get(KEY_MATCH_ID), archive.getTags().get(KEY_MATCH_ID));
            Assertions.assertEquals(entity.getSentContent(), archive.getSentContent());
            Assertions.assertEquals(entity.getSentTimestamp(), archive.getSentTimestamp());
            Assertions.assertEquals(entity.getTags(), archive.getTags());
            Assertions.assertEquals(Archive.Provider.valueOf(entity.getProvider().name()),
                    archive.getProvider());
            Assertions.assertEquals(Archive.Status.valueOf(entity.getStatus().name()),
                    archive.getStatus());
        }
    }

}
