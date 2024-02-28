package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uefa.platform.dto.message.LiveFeedMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.B2bLiveFeedData;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageMetadataProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util.ParameterValueResolver;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LiveFeedProcessorServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private FeedMessageProducer feedMessageProducer;

    private LiveFeedProcessorService service;

    @Mock
    private ParameterValueResolver parameterValueResolver;

    @Mock
    private LiveEventHandler liveEventHandler;

    @Mock
    //    private MessageArchiveBuilder messageArchiveBuilder;
    private MessageArchiveRepository messageArchiveRepository;
    @Mock
    private FeedMessageMetadataProducer feedMessageMetadataProducer;

    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        service = new LiveFeedProcessorService(feedRepository, clientRepository, feedMessageProducer, objectMapper,
                parameterValueResolver, liveEventHandler, feedMessageMetadataProducer);
    }

    @Test
    void testProcess() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);
        final Feed feedStatic =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing, feedStatic));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1),
                null, Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(),
                EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        when(clientRepository.updateLastSentDate(eq("id1"), eq("configId1"))).thenReturn(1L);

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);

        when(parameterValueResolver.getParameterValues(any(), any())).thenReturn(List.of("1"));
        when(feedMessageMetadataProducer.createMetadata(any(), any(), any(), any())).thenReturn(new HashMap<>());
        List<String> clientsSent =
                service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), null);

        Assertions.assertNotNull(clientsSent);
        Assertions.assertEquals(1, clientsSent.size());
        Assertions.assertEquals("Broadcaster", clientsSent.get(0));
        Mockito.verify(feedMessageMetadataProducer).createMetadata(any(), eq("FEED_1_CODE"), any(), eq(PlatformMessage.Type.B2B_LIVE_FEED));
        Mockito.verify(feedMessageProducer).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), eq(new HashMap<>()), eq(true));
        Mockito.verify(clientRepository, times(1)).updateLastSentDate(eq("id1"), eq("configId1"));
        Mockito.verify(liveEventHandler).processMessage(eq(data.getData()), eq(client));
        Mockito.verify(liveEventHandler, never()).processMessage(any(), eq(clientInactive));
    }

    @Test
    void testProcessWithConfigurationId() {
        String configId1 = "configId1";

        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);
        final Feed feedStatic =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing, feedStatic));

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration(configId1, "id1", List.of(parameter1),
                null, Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(),
                EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAllActiveClientsByConfigurationId(configId1)).thenReturn(List.of(clientInactive, client));
        when(clientRepository.updateLastSentDate("id1", configId1)).thenReturn(1L);

        when(parameterValueResolver.getParameterValues(any(), any())).thenReturn(List.of("1"));
        when(feedMessageMetadataProducer.createMetadata(any(), any(), any(), any())).thenReturn(new HashMap<>());

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        List<String> clientsSent =
                service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), configId1);

        Assertions.assertNotNull(clientsSent);
        Assertions.assertEquals(1, clientsSent.size());
        Assertions.assertEquals("Broadcaster", clientsSent.get(0));
        Mockito.verify(feedMessageMetadataProducer).createMetadata(any(), eq("FEED_1_CODE"), any(), eq(PlatformMessage.Type.B2B_LIVE_FEED));
        Mockito.verify(feedMessageProducer).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), eq(new HashMap<>()), eq(true));
        Mockito.verify(clientRepository, times(1)).updateLastSentDate("id1", configId1);
        Mockito.verify(liveEventHandler).processMessage(eq(data.getData()), eq(client));
        Mockito.verify(liveEventHandler, never()).processMessage(any(), eq(clientInactive));
    }

    @Test
    void testProcessNotFoundWithConfigurationId() {
        String configId1 = "configId1";

        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);
        final Feed feedStatic =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing, feedStatic));
        when(clientRepository.findAllActiveClientsByConfigurationId(configId1)).thenReturn(List.of());

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);

        List<String> clientsSent = service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), configId1);

        Assertions.assertNotNull(clientsSent);
        Assertions.assertEquals(0, clientsSent.size());
        Mockito.verify(feedMessageMetadataProducer, never()).createMetadata(any(), any(), any(), any());
        Mockito.verify(feedMessageProducer, never()).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), any(), anyBoolean());
        Mockito.verify(clientRepository, never()).updateLastSentDate("id1", configId1);
        Mockito.verify(liveEventHandler, never()).processMessage(any(), any());
    }

    @Test
    void testProcessParametersNotMatch() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);

        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                null, Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1), Status.ACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(client));

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "1"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), null);

        Mockito.verify(feedMessageMetadataProducer, never()).createMetadata(any(), any(), any(), any());
        Mockito.verify(feedMessageProducer, never()).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), any(), anyBoolean());
        Mockito.verify(clientRepository, never()).updateLastSentDate(eq("id1"), eq("configId1"));
        Mockito.verify(liveEventHandler).processMessage(eq(data.getData()), eq(client));
    }

    @Test
    void testProcessParametersWrong() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        LiveFeedDataType.COMPETITION_PLAYER_STATISTICS, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);

        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1),
                null, Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1), Status.ACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(client));

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "2"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), null);

        Mockito.verify(feedMessageMetadataProducer, never()).createMetadata(any(), any(), any(), any());
        Mockito.verify(feedMessageProducer, never()).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), any(), anyBoolean());
        Mockito.verify(clientRepository, never()).updateLastSentDate(eq("id1"), eq("configId1"));
        Mockito.verify(liveEventHandler).processMessage(eq(data.getData()), eq(client));
    }

    @Test
    void testProcessNoDataType() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, null,
                        Set.of("competitionId"), null, Instant.now().minus(10, ChronoUnit.MINUTES), null, FeedType.LIVE,
                        null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedForProcessing));

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);

        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1),
                null, Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1), Status.ACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(client));

        B2bLiveFeedData data = new B2bLiveFeedData(Map.of("competitionId", "2"), "{}");
        LiveFeedMessage message = new LiveFeedMessage(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE,
                PlatformMessage.Type.COMPETITION_PLAYER_STATISTICS,
                data);
        service.processMessage(message.getType(), message.getData().getMetadata(), message.getData().getData(), null);

        Mockito.verify(feedMessageMetadataProducer, never()).createMetadata(any(), any(), any(), any());
        Mockito.verify(feedMessageProducer, never()).send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_LIVE_FEED), any(), anyBoolean());
        Mockito.verify(clientRepository, never()).updateLastSentDate(eq("id1"), eq("configId1"));
        Mockito.verify(liveEventHandler, never()).processMessage(any(), any());
    }
}
