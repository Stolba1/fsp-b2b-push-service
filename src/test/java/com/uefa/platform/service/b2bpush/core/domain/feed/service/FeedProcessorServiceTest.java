package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.client.competition.v2.SeasonClient;
import com.uefa.platform.dto.competition.v2.Season;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.service.b2bpush.core.domain.archive.MessageArchive;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.MessageArchiveBuilder;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.repository.MessageArchiveRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageMetadataProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.FeedMessageProducer;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.util.ParameterValueResolver;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedConfigurationEntity;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Hash;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Hash.HashIdentifier;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessStaticFeedResult;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.HashRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedProcessorServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ConfigurationProcessorService configurationProcessorService;

    @Mock
    private ParameterValueResolver parameterValueResolver;

    @Mock
    private HashRepository hashRepository;

    @Mock
    private SeasonClient seasonClient;

    @Mock
    private FeedMessageProducer feedMessageProducer;

    @Mock
    private FeedMessageMetadataProducer feedMessageMetadataProducer;

    private FeedProcessorService service;

    @Mock
    private MessageArchiveBuilder messageArchiveBuilder;
    @Mock
    MessageArchiveRepository messageArchiveRepository;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new FeedProcessorService(feedRepository, clientRepository, configurationProcessorService,
                parameterValueResolver, hashRepository, seasonClient, feedMessageProducer, feedMessageMetadataProducer, messageArchiveBuilder);
    }


    @Test
    void testProcessStaticFeeds() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);

        ProcessStaticFeedResult result = new ProcessStaticFeedResult("Data", "Hash",
                Map.of("param1", "val1"), "https://comp-int.uefa.com/v1/teams?competitionId=1");
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(result);
        when(hashRepository.findById(any())).thenReturn(Optional.empty());
        when(feedMessageMetadataProducer.createMetadata(any(), any(), anyList(), any())).thenReturn(Map.of("key", "value"));

        service.processStaticFeeds();

        verify(feedMessageProducer, times(1))
                .send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_STATIC_FEED), any(), eq(true));

    }

    @Test
    void testProcessStaticCurrentSeasonFeeds() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "current", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);

        ProcessStaticFeedResult result = new ProcessStaticFeedResult("Data", "Hash",
                Map.of("param1", "val1"), "https://comp-int.uefa.com/v1/teams?competitionId=1");
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(result);
        when(hashRepository.findById(any())).thenReturn(Optional.empty());

        ArgumentCaptor<List<Client.FeedConfiguration.Parameter>> paramsCapture = ArgumentCaptor.forClass(List.class);
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(feedMessageMetadataProducer.createMetadata(any(), any(), paramsCapture.capture(), any())).thenReturn(Map.of("key", "value"));


        when(seasonClient.getCurrentSeason(eq("1")))
                .thenReturn(Mono.just(new Season("1", "2024", null, "1", null, null, "", "")));

        service.processStaticFeeds();

        verify(feedMessageProducer, times(1))
                .send(any(), eq("TEST_KEY"), eq(PlatformMessage.Type.B2B_STATIC_FEED), any(), eq(true));

        Assertions.assertNotNull(paramsCapture.getValue());
        int desiredParamsCount = 0;
        for (Client.FeedConfiguration.Parameter param : paramsCapture.getValue()) {
            if (param.getName().equals("competitionId")) {
                desiredParamsCount++;
                Assertions.assertEquals("1", param.getValue());
            } else if (param.getName().equals("seasonYear")) {
                desiredParamsCount++;
                Assertions.assertEquals("2024", param.getValue());
            }
        }
        Assertions.assertEquals(2, desiredParamsCount);
    }

    @Test
    void testProcessStaticFeedsSameHash() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);

        ProcessStaticFeedResult result = new ProcessStaticFeedResult("Data", "Hash",
                Map.of("param1", "val1"), "https://comp-int.uefa.com/v1/teams?competitionId=1");
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(result);
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(hashRepository.findById(any())).thenReturn(Optional.of(new Hash(new HashIdentifier("url", "1", "1"), "Hash", Instant.now())));

        service.processStaticFeeds();

        verify(feedMessageProducer, times(0))
                .send(any(), any(), any(), any(), anyBoolean());

    }

    @Test
    void testProcessStaticFeedsNoResult() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(null);
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), Instant.now(), null,
                        MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(null);

        service.processStaticFeeds();

        verify(feedMessageProducer, times(0))
                .send(any(), any(), any(), any(), anyBoolean());

    }
    @Test
    void testProcessStaticFeedsWithException() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);

        ProcessStaticFeedResult result = new ProcessStaticFeedResult("Data", "Hash",
                Map.of("param1", "val1"), "https://comp-int.uefa.com/v1/teams?competitionId=1");
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), null, Instant.now(),
                        null,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(configurationProcessorService.processStaticFeed(any())).thenReturn(result);
        when(hashRepository.findById(any())).thenThrow(new IllegalArgumentException("Error Message"));
        service.processStaticFeeds();
        Assertions.assertEquals(returnedMessageArchive.getStatus().name(), MessageArchiveEntity.Status.ERROR.name());
    }
    @Test
    void testProcessStaticFeedsWithIncorrectURLException() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);
        final Feed feedNotReady =
                new Feed("id2", "FEED_2_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 80, FeedType.STATIC, null, 30);
        final Feed feedInactive =
                new Feed("id3", "FEED_3_CODE", Status.INACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, FeedType.STATIC, null, 30);

        when(feedRepository.findAll()).thenReturn(List.of(feedInactive, feedNotReady, feedForProcessing));
        when(feedRepository.upsert(any(), eq("id1"))).thenReturn(null);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client.FeedConfiguration configuration1Inactive = new Client.FeedConfiguration("configId1Inactive", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.INACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1, configuration1Inactive), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientInactive =
                new Client("id2", "Broadcaster", "TEST_KEY_INACTIVE", List.of(configuration2), Status.INACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findAll()).thenReturn(List.of(clientInactive, client));
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put(parameter1.getName(), parameter1.getValue());
        paramMap.put(parameter2.getName(), parameter2.getValue());
        FeedConfigurationEntity activeConfig = new FeedConfigurationEntity("id1", "https://comp-int.uefa.com/v1/teams?competitionId={competitionId" +
                "}&seasonYear" +
                "={seasonYear}", paramMap, "FEED_1_CODE", "configId1", true);

        final Map<FeedConfigurationEntity, List<Client>> feedsWithClients = new HashMap<>();
        feedsWithClients.put(activeConfig, List.of(client));
        when(parameterValueResolver.calculateUniqueFeedsWithClients(anyList(), any())).thenReturn(feedsWithClients);

        ProcessStaticFeedResult result = new ProcessStaticFeedResult("Data", "Hash",
                Map.of("param1", "val1"), "https://comp-int.uefa.com/v1/teams?competitionId=1");
        MessageArchive returnedMessageArchive = new MessageArchive(messageArchiveRepository,
                new MessageArchiveEntity(UUID.randomUUID().toString(), null, Instant.now(),
                        null,
                        null, null, null, null, null, Instant.now(), "FEED_1_CODE", FeedType.STATIC));
        when(messageArchiveBuilder.buildStaticMessageArchive()).thenReturn(returnedMessageArchive);
        when(configurationProcessorService.processStaticFeed(any())).thenThrow(RestClientException.class);
        service.processStaticFeeds();
        Assertions.assertEquals(returnedMessageArchive.getStatus().name(), MessageArchiveEntity.Status.ERROR.name());
    }
}
