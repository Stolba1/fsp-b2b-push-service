package com.uefa.platform.service.b2bpush.core.domain.archive.data.repository;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CriteriaBuilder {

    private static final String KEY_TAGS = "tags";
    private static final String KEY_MATCH_ID = KEY_TAGS + "." + "matchId";
    private static final String KEY_HAS_LINEUP = KEY_TAGS + "." + "hasLineup";
    private static final String KEY_EVENT_IDS = KEY_TAGS + "." + "eventIds";
    private static final String KEY_SENT_TIMESTAMP = "sentTimestamp";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PROVIDER = "provider";

    private final Set<Criteria> criteriaSet;

    public CriteriaBuilder(@NotNull String matchId) {
        criteriaSet = new HashSet<>();
        criteriaSet.add(Criteria.where(KEY_MATCH_ID).is(matchId));
    }

    public CriteriaBuilder withProvider(MessageProvider provider) {
        if (Objects.nonNull(provider)) {
            criteriaSet.add(Criteria.where(KEY_PROVIDER).is(provider));
        }
        return this;
    }

    public CriteriaBuilder withStatus(Status status) {
        if (Objects.nonNull(status)) {
            criteriaSet.add(Criteria.where(KEY_STATUS).is(status));
        }
        return this;
    }

    public CriteriaBuilder withHasLineup(Boolean hasLineup) {
        if (Objects.nonNull(hasLineup)) {
            criteriaSet.add(Criteria.where(KEY_HAS_LINEUP).is(hasLineup));
        }
        return this;
    }

    public CriteriaBuilder withEventIds(Set<String> eventIds) {
        if (!CollectionUtils.isEmpty(eventIds)) {
            criteriaSet.add(Criteria.where(KEY_EVENT_IDS).in(eventIds));
        }
        return this;
    }

    public CriteriaBuilder withStartDate(Instant startDate) {
        if (Objects.nonNull(startDate)) {
            criteriaSet.add(Criteria.where(KEY_SENT_TIMESTAMP).gte(startDate));
        }
        return this;
    }

    public CriteriaBuilder withEndDate(Instant endDate) {
        if (Objects.nonNull(endDate)) {
            criteriaSet.add(Criteria.where(KEY_SENT_TIMESTAMP).lte(endDate));
        }
        return this;
    }

    public Criteria build() {
        return new Criteria().andOperator(criteriaSet.toArray(new Criteria[0]));
    }

}
