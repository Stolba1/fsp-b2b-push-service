package com.uefa.platform.service.b2bpush.core.domain.archive.data.model;

import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.Status;
import com.uefa.platform.web.controller.param.Limit;
import com.uefa.platform.web.controller.param.Offset;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Class to contain input data to then query for Archives
 */
public class ArchiveInputData {

    private final String matchId;
    private final MessageProvider provider;
    private final Status status;
    private final Boolean hasLineup;
    private final Set<String> eventIds;
    private final Instant startDate;
    private final Instant endDate;
    private final String text;
    private final Offset offset;
    private final Limit limit;

    private ArchiveInputData(String matchId, MessageProvider provider, Status status,
                             Boolean hasLineup, Set<String> eventIds,
                             Instant startDate, Instant endDate, String text,
                             Offset offset, Limit limit) {
        this.matchId = matchId;
        this.provider = provider;
        this.status = status;
        this.hasLineup = hasLineup;
        this.eventIds = eventIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.text = text;
        this.offset = offset;
        this.limit = limit;
    }

    public String getMatchId() {
        return matchId;
    }

    public MessageProvider getProvider() {
        return provider;
    }

    public Status getStatus() {
        return status;
    }

    public Boolean getHasLineup() {
        return hasLineup;
    }

    public Set<String> getEventIds() {
        return eventIds;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public String getText() {
        return text;
    }

    public Offset getOffset() {
        return offset;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "ArchiveInputData{" +
                "matchId='" + matchId + '\'' +
                ", provider=" + provider +
                ", status=" + status +
                ", hasLineup=" + hasLineup +
                ", eventIds=" + eventIds +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", text='" + text + '\'' +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArchiveInputData that = (ArchiveInputData) o;
        return matchId.equals(that.matchId) && status == that.status && Objects.equals(hasLineup, that.hasLineup) &&
                Objects.equals(eventIds, that.eventIds) && Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) && Objects.equals(text, that.text) && offset.equals(that.offset) &&
                limit.equals(that.limit) && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, status, hasLineup, provider,
                eventIds, startDate, endDate, text, offset, limit);
    }

    public static class Builder {

        private String matchId;
        private MessageProvider provider;
        private Status status;
        private Boolean hasLineup;
        private Set<String> eventIds;
        private Instant startDate;
        private Instant endDate;
        private String text;
        private Offset offset;
        private Limit limit;

        public Builder withMatchId(String matchId) {
            this.matchId = matchId;
            return this;
        }

        public Builder withProvider(MessageProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder withHasLineup(Boolean hasLineup) {
            this.hasLineup = hasLineup;
            return this;
        }

        public Builder withEventIds(Set<String> eventIds) {
            this.eventIds = eventIds;
            return this;
        }

        public Builder withStartDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(Instant endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Builder withOffset(Offset offset) {
            this.offset = offset;
            return this;
        }

        public Builder withLimit(Limit limit) {
            this.limit = limit;
            return this;
        }

        public ArchiveInputData build() {
            return new ArchiveInputData(
                    matchId, provider, status, hasLineup,
                    eventIds, startDate, endDate,
                    text, offset, limit
            );
        }

    }
}
