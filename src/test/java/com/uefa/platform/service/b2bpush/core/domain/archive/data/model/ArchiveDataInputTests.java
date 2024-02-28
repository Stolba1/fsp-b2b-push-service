package com.uefa.platform.service.b2bpush.core.domain.archive.data.model;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.web.controller.param.Limit;
import com.uefa.platform.web.controller.param.Offset;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchiveDataInputTests {

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(ArchiveInputData.class)
                .withNonnullFields("matchId", "offset", "limit")
                .verify();
    }

    @Test
    void testBuilder() {
        final String matchId = "12345";
        final MessageArchiveEntity.Status status = Status.SUCCESS;
        final Boolean hasLineup = false;
        final Set<String> eventIds = Set.of("event-X", "event-Y");
        final Instant startDate = Instant.now();
        final Instant endDate = Instant.now().plus(1, ChronoUnit.DAYS);
        final String text = "search me";
        final Offset offset = Offset.of("5");
        final Limit limit = Limit.of("15");

        ArchiveInputData test = new ArchiveInputData.Builder()
                .withMatchId(matchId)
                .withStatus(status)
                .withHasLineup(hasLineup)
                .withEventIds(eventIds)
                .withStartDate(startDate)
                .withEndDate(endDate)
                .withText(text)
                .withOffset(offset)
                .withLimit(limit)
                .build();

        assertEquals(matchId, test.getMatchId());
        assertEquals(status, test.getStatus());
        assertEquals(hasLineup, test.getHasLineup());
        assertEquals(eventIds, test.getEventIds());
        assertEquals(startDate, test.getStartDate());
        assertEquals(endDate, test.getEndDate());
        assertEquals(text, test.getText());
        assertEquals(offset, test.getOffset());
        assertEquals(limit, test.getLimit());
    }

}
