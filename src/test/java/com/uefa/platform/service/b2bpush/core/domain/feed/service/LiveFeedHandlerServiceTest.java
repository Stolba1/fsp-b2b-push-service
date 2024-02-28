package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.competition.v2.CompetitionPhase;
import com.uefa.platform.dto.match.v2.Match;
import com.uefa.platform.dto.message.LiveFeedMessage;
import com.uefa.platform.dto.message.MatchStateMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bLiveFeedData;
import com.uefa.platform.dto.message.matchstate.MatchInfo;
import com.uefa.platform.dto.message.matchstate.MatchInfoWrapper;
import com.uefa.platform.dto.message.matchstate.MatchState;
import com.uefa.platform.dto.message.matchstate.MatchStateData;
import com.uefa.platform.dto.message.matchstate.MatchStateWrapper;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LiveFeedHandlerServiceTest {

    @Mock
    private MessageArchiveBuilder messageArchiveBuilder;

    @Mock
    private LiveFeedProcessorService liveFeedProcessorService;

    @Mock
    private FeedRepository feedRepository;
    private LiveFeedHandlerService liveFeedHandlerService;

    @Captor
    ArgumentCaptor<MessageArchiveEntity> messageArchiveCaptor;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        liveFeedHandlerService = new LiveFeedHandlerService(messageArchiveBuilder, objectMapper, liveFeedProcessorService, feedRepository);
    }

    @Test
    void testHandleMessage() {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);
        Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.MATCH_STATE, 10);
        when(feedRepository.findActiveByLiveDataType(any())).thenReturn(Optional.of(feedForProcessing));
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.LIVE));

        when(messageArchiveBuilder.build(any(), any(), any())).thenReturn(returnedMessageArchive);

        when(liveFeedProcessorService.processMessage(any(), any(), anyString(), any())).thenReturn(List.of("client1"));
        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message =
                new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE, PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, data);

        liveFeedHandlerService.handleMessage(message, null);


        Mockito.verify(liveFeedProcessorService)
                .processMessage(PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, Map.of("competitionId", "1"), message.getData().getData(), null);
        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE), eq(null), any());

        Mockito.verify(messageArchiveRepository).save(messageArchiveCaptor.capture());
        MessageArchiveEntity archive = messageArchiveCaptor.getValue();
        Assertions.assertNotNull(archive);
        Assertions.assertNotNull(archive.getDeleteDate());
        Assertions.assertNotNull(archive.getFeedName());
        Assertions.assertNotNull(archive.getFeedType());
        Assertions.assertTrue(archive.getDeleteDate().isAfter(Instant.now().plus(9, ChronoUnit.DAYS)));
        Assertions.assertTrue(archive.getDeleteDate().isBefore(Instant.now().plus(11, ChronoUnit.DAYS)));

    }

    @Test
    void testHandleMessageNoRetention() {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);
        when(feedRepository.findActiveByLiveDataType(any())).thenReturn(Optional.empty());
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), null, null));

        when(messageArchiveBuilder.build(any(), any(), any())).thenReturn(returnedMessageArchive);

        when(liveFeedProcessorService.processMessage(any(), any(), anyString(), any())).thenReturn(List.of("client1"));
        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message =
                new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE, PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, data);

        liveFeedHandlerService.handleMessage(message, null);


        Mockito.verify(liveFeedProcessorService)
                .processMessage(PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, Map.of("competitionId", "1"), message.getData().getData(), null);
        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE), eq(null), any());

        Mockito.verify(messageArchiveRepository).save(messageArchiveCaptor.capture());
        MessageArchiveEntity archive = messageArchiveCaptor.getValue();
        Assertions.assertNotNull(archive);
        Assertions.assertNotNull(archive.getDeleteDate());
        Assertions.assertTrue(archive.getDeleteDate().isAfter(Instant.now().plus(29, ChronoUnit.DAYS)));
        Assertions.assertTrue(archive.getDeleteDate().isBefore(Instant.now().plus(31, ChronoUnit.DAYS)));

    }

    @Test
    void testHandleMessageNullRetention() {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);
        Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.MATCH_STATE, null);
        when(feedRepository.findActiveByLiveDataType(any())).thenReturn(Optional.of(feedForProcessing));
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), null, null));

        when(messageArchiveBuilder.build(any(), any(), any())).thenReturn(returnedMessageArchive);

        when(liveFeedProcessorService.processMessage(any(), any(), anyString(), any())).thenReturn(List.of("client1"));
        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message =
                new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE, PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, data);

        liveFeedHandlerService.handleMessage(message, null);


        Mockito.verify(liveFeedProcessorService)
                .processMessage(PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, Map.of("competitionId", "1"), message.getData().getData(), null);
        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE), eq(null), any());

        Mockito.verify(messageArchiveRepository).save(messageArchiveCaptor.capture());
        MessageArchiveEntity archive = messageArchiveCaptor.getValue();
        Assertions.assertNotNull(archive);
        Assertions.assertNotNull(archive.getDeleteDate());
        Assertions.assertTrue(archive.getDeleteDate().isAfter(Instant.now().plus(29, ChronoUnit.DAYS)));
        Assertions.assertTrue(archive.getDeleteDate().isBefore(Instant.now().plus(31, ChronoUnit.DAYS)));

    }

    @Test
    void testHandleMessageException() {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        MessageArchive archive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(id, now, null, MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE, null, null, null, null, null,
                        null, null, null));

        when(messageArchiveBuilder.build(any(), any(), any())).thenReturn(archive);

        when(liveFeedProcessorService.processMessage(any(), any(), anyString(), any())).thenThrow(new NullPointerException());
        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message =
                new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE, PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, data);

        liveFeedHandlerService.handleMessage(message, null);

        Mockito.verify(liveFeedProcessorService)
                .processMessage(PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS, Map.of("competitionId", "1"), message.getData().getData(), null);
        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE), eq(null), any());
        Mockito.verify(messageArchiveRepository).save(any());

    }

    @Test
    void testHandleMatchStateMessage() throws JsonProcessingException {
        MessageArchiveRepository messageArchiveRepository = mock(MessageArchiveRepository.class);
        when(messageArchiveRepository.save(any())).thenReturn(null);
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null, MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, null,
                        null, null, null, null, null, null, null));
        when(messageArchiveBuilder.build(any(), anyString(), any())).thenReturn(returnedMessageArchive);

        when(liveFeedProcessorService.processMessage(any(), any(), any(), any())).thenReturn(List.of("client1"));
        MatchState matchState = new MatchState("matchId", Match.Status.FINISHED, null, null, null, null, null, null, null, null, null);
        MatchInfo matchInfo =
                new MatchInfo("matchId", "1", "2022", null, null, null, null, null, Instant.now(), null, null, null,
                        null, null, CompetitionPhase.QUALIFYING, null,
                        null, null, null, null, null);
        MatchInfoWrapper matchInfoWrapper = new MatchInfoWrapper(matchInfo, null);
        MatchStateData data = new MatchStateData(null, new MatchStateWrapper(matchState, null), matchInfoWrapper, null, null);
        MatchStateMessage message = new MatchStateMessage(PlatformMessage.Action.B2BPushService.UPDATE, data);

        liveFeedHandlerService.handleMessage(message, null);


        Mockito.verify(liveFeedProcessorService).processMessage(eq(PlatformMessage.Type.MATCH_STATE),
                eq(Map.of("competitionId", "1", "seasonYear", "2022", "matchId", "matchId")),
                any(), eq(null));

        Mockito.verify(messageArchiveBuilder)
                .build(eq(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER), eq(objectMapper.writeValueAsString(message)), any());
        Mockito.verify(messageArchiveRepository).save(any());

    }

}
