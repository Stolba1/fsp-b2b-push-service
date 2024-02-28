package com.uefa.platform.service.b2bpush.core.domain.matchstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.B2bMatchStateMessage;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.b2b.B2bMatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.MatchStateDataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.feed.service.LiveFeedHandlerService.DEFAULT_RETENTION_DAYS;

@Service
public class MatchStateUpdateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MatchStateUpdateHandler.class);

    private final MatchStateMessageProducer producer;
    private final MatchStateDataConverter converter;
    private final MessageArchiveBuilder messageArchiveBuilder;
    private final ObjectMapper objectMapper;

    public MatchStateUpdateHandler(MatchStateMessageProducer producer, MatchStateDataConverter converter,
                                   MessageArchiveBuilder messageArchiveBuilder, ObjectMapper objectMapper) {
        this.producer = producer;
        this.converter = converter;
        this.messageArchiveBuilder = messageArchiveBuilder;
        this.objectMapper = objectMapper;
    }

    public void handleUpdate(MatchStateMessage matchStateMessage) {
        final Instant receivedAt = Instant.now();
        MessageArchive archive = null;

        try {
            archive = messageArchiveBuilder.build(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER,
                    objectMapper.writeValueAsString(matchStateMessage), receivedAt);

            final MatchStateData data = matchStateMessage.getData().copyWithoutProviderData();

            // comment the enrichment of the b2b push messages as part of the following story https://jira.uefa.com/browse/FSP-2068
            //B2bMatchStateData converted = converter.convert(data);
            B2bMatchStateData converted = new B2bMatchStateData(data, null, null, null);

            producer.send(new B2bMatchStateMessage(converted));

            archive.withStatus(MessageArchiveEntity.Status.SUCCESS)
                    .withTag(ArchiveTags.MATCH_ID, converted.getMatchStateData().getMatchInfo().getData().getId())
                    .withTag(ArchiveTags.HAS_LINEUP, converted.getMatchStateData().getLineupUpdate() != null)
                    .withSentContent(objectMapper.writeValueAsString(converted))
                    .withSentTimestamp(Instant.now());
            if (converted.getMatchStateData().getEventUpdates() != null) {
                final Set<String> eventIds = converted.getMatchStateData().getEventUpdates().stream()
                        .map(MatchStateData.MatchEventWrapper::getEventId).collect(Collectors.toSet());
                archive.withTag(ArchiveTags.EVENT_IDS, eventIds);
            }
            if (converted.getMatchStateData().getOfficialsUpdate() != null) {
                archive.withTag(ArchiveTags.HAS_OFFICIALS, true);

            }
            archive.withDeleteDate(receivedAt.plus(DEFAULT_RETENTION_DAYS, ChronoUnit.DAYS));

        } catch (Exception e) {
            LOG.error(String.format("Error handling Match State provider message with archiveId:%s", archive != null ? archive.getArchiveId() : null), e);
            if (archive != null) {
                archive.withException(e.getClass() + ":" + e.getMessage())
                        .withStatus(MessageArchiveEntity.Status.ERROR);

            }
        } finally {
            if (archive != null) {
                archive.save();
            }
        }
    }
}
