package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.dto.message.B2bFeedMessage;
import com.uefa.platform.dto.message.CommandMessage;
import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.b2b.CommandData;
import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.FeedConfigurationDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.ParameterDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.LiveFeedDataType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardClientControllerTest extends AbstractIntegrationTest {
    private static final String PATH = "/v1/dashboard/clients";

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FeedRepository feedRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<B2bFeedMessage> argumentCaptor;

    @Captor
    private ArgumentCaptor<CommandMessage> commandMessageargumentCaptor;

    @BeforeEach
    void setUp() {
        feedRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void testSaveClient() {
        String feedId = "123";
        String routingKey = "routing key";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        feedRepository.save(feed);

        DashboardClientDTO clientDTO = getClientDTO(feedId, routingKey);

        post(PATH, clientDTO, responseOk())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.is(clientDTO.getName()))
                .body("routingKey", Matchers.is(clientDTO.getRoutingKey()))
                .body("status", Matchers.is(clientDTO.getStatus().name()))
                .body("lastUpdatedTime", Matchers.nullValue())
                .body("configurations.size()", Matchers.is(clientDTO.getConfigurations().size()))
                .body("configurations[0].id", Matchers.notNullValue())
                .body("configurations[0].feedId", Matchers.is(feedId))
                .body("configurations[0].status", Matchers.is(clientDTO.getConfigurations().get(0).getStatus().name()))
                .body("configurations[0].lastSentTime", Matchers.nullValue())
                .body("configurations[0].parameters[0].name", Matchers.is(clientDTO.getConfigurations().get(0).getParameters().get(0).getName()))
                .body("configurations[0].parameters[0].value", Matchers.is(clientDTO.getConfigurations().get(0).getParameters().get(0).getValue()))
                .body("eventPackage", Matchers.is(clientDTO.getEventPackage().name()));
    }

    @Test
    void testSaveClientWhenFeedNotExists() {
        String feedId = "123";
        String routingKey = "routing key";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        feedRepository.save(feed);

        ParameterDTO parameterDTO = new ParameterDTO("name param", "value param", null, null);
        FeedConfigurationDTO feedConfigurationDTO1 = new FeedConfigurationDTO(null, feedId, List.of(parameterDTO),
                Status.ACTIVE, Instant.now(), true);
        FeedConfigurationDTO feedConfigurationDTO2 = new FeedConfigurationDTO(null, "not existing feed", List.of(parameterDTO),
                Status.ACTIVE, Instant.now(), true);
        DashboardClientDTO clientDTO = new DashboardClientDTO(null, "name", routingKey,
                Status.ACTIVE, List.of(feedConfigurationDTO1, feedConfigurationDTO2), Instant.now(), EventPackage.BASIC);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        post(PATH, clientDTO, response)
                .body("error.message",
                        Matchers.is("No feed found for feedId: not existing feed"));
    }

    @Test
    void testClientAlreadyExistsInDb() {
        String feedId = "123";
        String routingKey = "routing key";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client("456", "name", routingKey,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        DashboardClientDTO clientDTO = getClientDTO(feedId, routingKey);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        post(PATH, clientDTO, response)
                .body("error.message",
                        Matchers.is("Client with routingKey: routing key found in repository"));
    }

    @Test
    void testUnauthorizedUser() {
        String feedId = "123";
        String routingKey = "routing key";
        DashboardClientDTO clientDTO = getClientDTO(feedId, routingKey);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        postNoAuth(PATH, clientDTO, response);
    }

    @Test
    void testGetClientById() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Client.FeedConfiguration feedConfiguration =
                new Client.FeedConfiguration("555", feedId, List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                        "hash", Instant.now(), Status.ACTIVE, false);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration),
                Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        get(PATH + "/" + clientId, responseOk())
                .body("id", Matchers.is(client.getId()))
                .body("name", Matchers.is(client.getName()))
                .body("routingKey", Matchers.is(client.getRoutingKey()))
                .body("status", Matchers.is(client.getStatus().name()))
                .body("lastUpdatedTime", Matchers.nullValue())
                .body("configurations.size()", Matchers.is(client.getConfigurations().size()))
                .body("configurations[0].id", Matchers.is(feedConfiguration.getId()))
                .body("configurations[0].feedId", Matchers.is(feedId))
                .body("configurations[0].status", Matchers.is(feedConfiguration.getStatus().name()))
                .body("configurations[0].lastSentTime", Matchers.notNullValue())
                .body("configurations[0].payloadSharedToClient", Matchers.is(false))
                .body("configurations[0].parameters[0].name", Matchers.is(feedConfiguration.getParameters().get(0).getName()))
                .body("configurations[0].parameters[0].value", Matchers.is(feedConfiguration.getParameters().get(0).getValue()))
                .body("eventPackage", Matchers.is(client.getEventPackage().name()));
    }

    @Test
    void testGetClientByIdNotExists() {
        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        get(PATH + "/123", response);
    }

    @Test
    void testGetAllClients() {
        String feedId1 = "123";
        String clientId1 = "456";
        String routingKey1 = "routing key";
        String feedId2 = "777";
        String clientId2 = "888";
        String routingKey2 = "routing 2";
        Client client1 = new Client(clientId1, "name b", routingKey1,
                List.of(new Client.FeedConfiguration("555", feedId1, List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                        "hash", Instant.now(), Status.ACTIVE, true)),
                Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client client2 = new Client(clientId2, "name a", routingKey2,
                List.of(new Client.FeedConfiguration("333", feedId2, List.of(new Client.FeedConfiguration.Parameter("name param", "value param", null, null)),
                        "hash", Instant.now(), Status.ACTIVE, false)),
                Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);
        clientRepository.saveAll(List.of(client1, client2));

        get(PATH, responseOk())
                .body("[0].id", Matchers.is(client2.getId()))
                .body("[0].name", Matchers.is(client2.getName()))
                .body("[0].routingKey", Matchers.is(client2.getRoutingKey()))
                .body("[0].status", Matchers.is(client2.getStatus().name()))
                .body("[0].lastUpdatedTime", Matchers.nullValue())
                .body("[0].configurations.size()", Matchers.is(client2.getConfigurations().size()))
                .body("[0].configurations[0].feedId", Matchers.is(feedId2))
                .body("[1].id", Matchers.is(client1.getId()))
                .body("[1].name", Matchers.is(client1.getName()))
                .body("[1].routingKey", Matchers.is(client1.getRoutingKey()))
                .body("[1].status", Matchers.is(client1.getStatus().name()))
                .body("[1].lastUpdatedTime", Matchers.nullValue())
                .body("[1].configurations.size()", Matchers.is(client1.getConfigurations().size()))
                .body("[1].configurations[0].feedId", Matchers.is(feedId1));
    }

    @Test
    void testGetAllClientsReturnsEmptyList() {
        get(PATH, responseOk())
                .body("size()", Matchers.is(0));
    }

    @Test
    void testUpdateClient() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        String routingKeyNew = "routing key new";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);
        feedRepository.save(feed);
        clientRepository.save(client);

        DashboardClientDTO clientDTO = getClientDTO(feedId, routingKeyNew);

        put(PATH + "/456", clientDTO, responseOk())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.is(clientDTO.getName()))
                .body("routingKey", Matchers.is(clientDTO.getRoutingKey()))
                .body("status", Matchers.is(clientDTO.getStatus().name()))
                .body("lastUpdatedTime", Matchers.nullValue())
                .body("configurations.size()", Matchers.is(clientDTO.getConfigurations().size()))
                .body("configurations[0].feedId", Matchers.is(feedId))
                .body("eventPackage", Matchers.is(clientDTO.getEventPackage().name()));

        Optional<Client> savedClient = clientRepository.findById(clientId);
        Assertions.assertTrue(savedClient.isPresent());
        Assertions.assertEquals(clientId, savedClient.get().getId());
        Assertions.assertEquals(clientDTO.getName(), savedClient.get().getName());
        Assertions.assertEquals(clientDTO.getRoutingKey(), savedClient.get().getRoutingKey());
        Assertions.assertEquals(clientDTO.getStatus(), savedClient.get().getStatus());
        Assertions.assertEquals(feedId, savedClient.get().getConfigurations().get(0).getFeedId());
        Assertions.assertEquals(clientDTO.getEventPackage(), savedClient.get().getEventPackage());
    }

    @Test
    void testUpdateClientConfiguration() {
        String feedId1 = "111";
        String feedId2 = "222";
        String feedId3 = "333";
        String clientId = "456";
        String configId1 = "666";
        String routingKey = "routing key";

        Feed feed1 = new Feed(feedId1, "MATCH_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Feed feed2 = new Feed(feedId2, "TEAM_V2", Status.ACTIVE,
                "url2", Set.of("param2"), null, Instant.now(), 10, null, null, 30);
        Feed feed3 = new Feed(feedId3, "TEAM_V3", Status.INACTIVE,
                "url3", Set.of("param3"), null, Instant.now(), 20, null, null, 30);
        Client.FeedConfiguration existingFeedConfig1 = new Client.FeedConfiguration(configId1, feedId1,
                List.of(new Client.FeedConfiguration.Parameter("name", "value", null, null)),
                "hash value", Instant.now(), Status.ACTIVE, true);
        Client.FeedConfiguration existingFeedConfig2 = new Client.FeedConfiguration("777", feedId2,
                List.of(new Client.FeedConfiguration.Parameter("name 2", "value 2", null, null)),
                "hash value 2", null, Status.ACTIVE, true);
        Client client = new Client(clientId, "name", routingKey,
                List.of(existingFeedConfig1, existingFeedConfig2),
                Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.saveAll(List.of(feed1, feed2, feed3));
        clientRepository.save(client);


        ParameterDTO parameterDTO1 = new ParameterDTO("update name", "updated value", null, null);
        ParameterDTO parameterDTO2 = new ParameterDTO("update name 1", "updated value 1", null, null);
        FeedConfigurationDTO feedConfigurationDTO1 = new FeedConfigurationDTO(configId1, feedId1, List.of(parameterDTO1),
                Status.ACTIVE, null, true);
        FeedConfigurationDTO feedConfigurationDTO2 = new FeedConfigurationDTO(null, feedId3, List.of(parameterDTO2),
                Status.ACTIVE, Instant.now().minusSeconds(1000), true);
        DashboardClientDTO clientDTO = new DashboardClientDTO(null, "name", routingKey,
                Status.INACTIVE, List.of(feedConfigurationDTO1, feedConfigurationDTO2), Instant.now(), EventPackage.BASIC);

        put(PATH + "/456", clientDTO, responseOk())
                .body("configurations.size()", Matchers.is(2))
                .body("configurations[0].id", Matchers.is(configId1))
                .body("configurations[0].feedId", Matchers.is(feedId1))
                .body("configurations[0].status", Matchers.is(existingFeedConfig1.getStatus().name()))
                .body("configurations[0].parameters[0].name", Matchers.is(feedConfigurationDTO1.getParameters().get(0).getName()))
                .body("configurations[0].parameters[0].value", Matchers.is(feedConfigurationDTO1.getParameters().get(0).getValue()))
                .body("configurations[1].id", Matchers.notNullValue())
                .body("configurations[1].feedId", Matchers.is(feedId3))
                .body("configurations[1].status", Matchers.is(existingFeedConfig1.getStatus().name()))
                .body("configurations[1].lastSentTime", Matchers.nullValue())
                .body("configurations[1].parameters[0].name", Matchers.is(feedConfigurationDTO2.getParameters().get(0).getName()))
                .body("configurations[1].parameters[0].value", Matchers.is(feedConfigurationDTO2.getParameters().get(0).getValue()));

        Optional<Client> savedClient = clientRepository.findById(clientId);
        Assertions.assertTrue(savedClient.isPresent());
        Assertions.assertEquals(2, savedClient.get().getConfigurations().size());
        Assertions.assertEquals(configId1, savedClient.get().getConfigurations().get(0).getId());
        Assertions.assertEquals(feedId1, savedClient.get().getConfigurations().get(0).getFeedId());
        Assertions.assertEquals(existingFeedConfig1.getHash(), savedClient.get().getConfigurations().get(0).getHash());
        Assertions.assertEquals(existingFeedConfig1.getStatus(), savedClient.get().getConfigurations().get(0).getStatus());
        Assertions.assertEquals(existingFeedConfig1.getLastSentTime().truncatedTo(ChronoUnit.SECONDS),
                savedClient.get().getConfigurations().get(0).getLastSentTime().truncatedTo(ChronoUnit.SECONDS));
        Assertions.assertEquals(feedConfigurationDTO1.getParameters().get(0).getName(),
                savedClient.get().getConfigurations().get(0).getParameters().get(0).getName());
        Assertions.assertEquals(feedConfigurationDTO1.getParameters().get(0).getValue(),
                savedClient.get().getConfigurations().get(0).getParameters().get(0).getValue());

        Assertions.assertNotNull(savedClient.get().getConfigurations().get(1).getId());
        Assertions.assertEquals(feedId3, savedClient.get().getConfigurations().get(1).getFeedId());
        Assertions.assertEquals(existingFeedConfig2.getStatus(), savedClient.get().getConfigurations().get(1).getStatus());
        Assertions.assertNull(savedClient.get().getConfigurations().get(1).getHash());
        Assertions.assertNull(savedClient.get().getConfigurations().get(1).getHash());
        Assertions.assertEquals(feedConfigurationDTO2.getParameters().get(0).getName(),
                savedClient.get().getConfigurations().get(1).getParameters().get(0).getName());
        Assertions.assertEquals(feedConfigurationDTO2.getParameters().get(0).getValue(),
                savedClient.get().getConfigurations().get(1).getParameters().get(0).getValue());
    }

    @Test
    void testUpdateClientAlreadyExistsInDb() {
        String routingKeyOld = "routing key";
        String routingKeyNew = "routing key new";
        Client clientToBeUpdated = new Client("222", "name", routingKeyOld,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client clientWithSameRoutingKey = new Client("333", "name", routingKeyNew,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);
        clientRepository.saveAll(List.of(clientToBeUpdated, clientWithSameRoutingKey));

        DashboardClientDTO clientDTO = getClientDTO("1111", routingKeyNew);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        put(PATH + "/222", clientDTO, response)
                .body("error.message",
                        Matchers.is("Client with routingKey: routing key new found in repository"));
    }

    @Test
    void testChangeClientStatus() {
        String clientId = "456";
        String routingKey = "routing key";
        Client client = new Client(clientId, "name", routingKey,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        clientRepository.save(client);

        ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();
        patch(PATH + "/456?status=INACTIVE", response);

        Optional<Client> savedClient = clientRepository.findById(clientId);
        Assertions.assertTrue(savedClient.isPresent());
        Assertions.assertEquals(client.getId(), savedClient.get().getId());
        Assertions.assertEquals(client.getName(), savedClient.get().getName());
        Assertions.assertEquals(client.getRoutingKey(), savedClient.get().getRoutingKey());
        Assertions.assertEquals(Status.INACTIVE, savedClient.get().getStatus());
    }

    @Test
    void testDeleteClientById() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "url", Set.of("param1"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        delete(PATH + "/456", response);
        Optional<Client> deletedClient = clientRepository.findById(clientId);
        Assertions.assertFalse(deletedClient.isPresent());
    }

    @Test
    void testDeleteClientByIdNotExists() {
        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        delete(PATH + "/123", response)
                .body("error.message", Matchers.is("No client found for clientId:123"));

    }

    @Test
    void testBootstrap() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                Set.of("competitionId", "seasonYear"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        String url = "https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2020";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(""));

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);

        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), argumentCaptor.capture(), any(MessagePostProcessor.class));
        B2bFeedMessage feedPushData = argumentCaptor.getValue();
        Assertions.assertEquals(feed.getCode(), feedPushData.getData().getInfo().getFeedName());
        Assertions.assertEquals("competitionId", feedPushData.getData().getInfo().getParameters().get(0).getName());
        Assertions.assertEquals("1", feedPushData.getData().getInfo().getParameters().get(0).getValue());
        Assertions.assertEquals("seasonYear", feedPushData.getData().getInfo().getParameters().get(1).getName());
        Assertions.assertEquals("2020", feedPushData.getData().getInfo().getParameters().get(1).getValue());
    }

    @Test
    void testBootstrapLiveFeeds() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                Set.of("competitionId", "seasonYear"), Set.of("matchId"), Instant.now(), 5,
                FeedType.LIVE, LiveFeedDataType.MATCH_TEAM_STATISTICS, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        String url = "https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2020";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(""));

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap" + "?matchId=123456", response);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("b2b-push-service-commands-exchange"), eq("MATCH-STATISTICS"),
                commandMessageargumentCaptor.capture());
        CommandMessage commandMessage = commandMessageargumentCaptor.getValue();
        Assertions.assertEquals(PlatformMessage.Type.COMMAND, commandMessage.getType());
        Assertions.assertEquals(PlatformMessage.Provider.B2B_PUSH, commandMessage.getProvider());
        Assertions.assertEquals("B2B-PUSH", commandMessage.getData().getCommandProducerRoutingKey());
        Assertions.assertEquals(feedConfiguration.getId(), commandMessage.getData().getClientFeedConfigurationId());
        Assertions.assertEquals(CommandData.CommandDetails.MATCH_TEAM_STATISTICS, commandMessage.getData().getCommandDetails());
        Assertions.assertEquals(CommandData.Type.BOOTSTRAP, commandMessage.getData().getCommandType());
        Assertions.assertEquals(Map.of("competitionId", "1", "seasonYear", "2020", "matchId", "123456"),
                commandMessage.getData().getCommandParameters());
    }

    @Test
    void testBootstrapLiveFeedsNoBootstrapParams() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                Set.of("competitionId", "seasonYear"), null, Instant.now(), 5,
                FeedType.LIVE, LiveFeedDataType.MATCH_TEAM_STATISTICS, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        String url = "https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2020";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(""));

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("b2b-push-service-commands-exchange"), eq("MATCH-STATISTICS"),
                commandMessageargumentCaptor.capture());
        CommandMessage commandMessage = commandMessageargumentCaptor.getValue();
        Assertions.assertEquals(PlatformMessage.Type.COMMAND, commandMessage.getType());
        Assertions.assertEquals(PlatformMessage.Provider.B2B_PUSH, commandMessage.getProvider());
        Assertions.assertEquals("B2B-PUSH", commandMessage.getData().getCommandProducerRoutingKey());
        Assertions.assertEquals(feedConfiguration.getId(), commandMessage.getData().getClientFeedConfigurationId());
        Assertions.assertEquals(CommandData.CommandDetails.MATCH_TEAM_STATISTICS, commandMessage.getData().getCommandDetails());
        Assertions.assertEquals(CommandData.Type.BOOTSTRAP, commandMessage.getData().getCommandType());
        Assertions.assertEquals(Map.of("competitionId", "1", "seasonYear", "2020"),
                commandMessage.getData().getCommandParameters());
    }

    @Test
    void testBootstrapMultipleTimesNoHashChanged() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Feed feed = new Feed(feedId, "TEAM_V2", Status.ACTIVE,
                "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                Set.of("competitionId", "seasonYear"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        String url = "https://comp-int.uefa.com/v1/teams?competitionId=1&seasonYear=2020";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(""));

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);
        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);

        verify(rabbitTemplate, times(2)).convertAndSend(any(), any(), argumentCaptor.capture(), any(MessagePostProcessor.class));
        B2bFeedMessage feedPushData = argumentCaptor.getValue();
        Assertions.assertEquals(feed.getCode(), feedPushData.getData().getInfo().getFeedName());
        Assertions.assertEquals("competitionId", feedPushData.getData().getInfo().getParameters().get(0).getName());
        Assertions.assertEquals("1", feedPushData.getData().getInfo().getParameters().get(0).getValue());
        Assertions.assertEquals("seasonYear", feedPushData.getData().getInfo().getParameters().get(1).getName());
        Assertions.assertEquals("2020", feedPushData.getData().getInfo().getParameters().get(1).getValue());
    }

    @Test
    void testBootstrapNoActiveClient() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.INACTIVE, Instant.now(), EventPackage.BASIC);
        clientRepository.save(client);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response)
                .body("error.message", Matchers.is("No client found with configurationId: 555"));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), any(B2bFeedMessage.class), any(MessagePostProcessor.class));
    }

    @Test
    void testBootstrapNoClientForConfigurationId() {
        String clientId = "456";
        String routingKey = "routing key";
        Client client = new Client(clientId, "name", routingKey,
                List.of(), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        clientRepository.save(client);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.NOT_FOUND.value())
                .build();
        post(PATH + "/configuration/" + "555" + "/bootstrap", response)
                .body("error.message", Matchers.is("No client found with configurationId: 555"));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), any(B2bFeedMessage.class), any(MessagePostProcessor.class));
    }

    @Test
    void testBootstrapInactiveConfiguration() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.INACTIVE, true);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        clientRepository.save(client);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();
        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), any(B2bFeedMessage.class), any(MessagePostProcessor.class));
    }

    @Test
    void testBootstrapInactiveFeed() {
        String feedId = "123";
        String clientId = "456";
        String routingKey = "routing key";
        Client.FeedConfiguration feedConfiguration = new Client.FeedConfiguration("555", feedId,
                List.of(new Client.FeedConfiguration.Parameter("competitionId", "1", null, null),
                        new Client.FeedConfiguration.Parameter("seasonYear", "2020", null, null)),
                "hash", Instant.now(), Status.ACTIVE, true);
        Feed feed = new Feed(feedId, "TEAM_V2", Status.INACTIVE,
                "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                Set.of("competitionId", "seasonYear"), null, Instant.now(), 5, null, null, 30);
        Client client = new Client(clientId, "name", routingKey,
                List.of(feedConfiguration), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        feedRepository.save(feed);
        clientRepository.save(client);

        final ResponseSpecification response = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .build();

        post(PATH + "/configuration/" + feedConfiguration.getId() + "/bootstrap", response);
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), any(B2bFeedMessage.class), any(MessagePostProcessor.class));
    }

    @Test
    void testGetAggregators() {
        get(PATH + "/aggregators", responseOk())
                .body("size()", Matchers.is(2))
                .body("[0].name", Matchers.is("ROUND_ID_AGGREGATOR"))
                .body("[0].parameters[0]", Matchers.is("competitionId"))
                .body("[0].parameters[1]", Matchers.is("seasonYear"))
                .body("[1].name", Matchers.is("TEAM_ID_AGGREGATOR"))
                .body("[1].parameters[0]", Matchers.is("competitionId"))
                .body("[1].parameters[1]", Matchers.is("seasonYear"));
    }

    @DisplayName("Trying to query from Data Explorer")
    @Test
    void testQueryFromDataExplorer() {
        final String clientPath = PATH + "/456";

        getForDataExplorer(clientPath, withResponse(HttpStatus.FORBIDDEN));
    }

    @NotNull
    private DashboardClientDTO getClientDTO(String feedId, String routingKey) {
        ParameterDTO parameterDTO = new ParameterDTO("name param", "value param", null, null);
        FeedConfigurationDTO feedConfigurationDTO = new FeedConfigurationDTO(null, feedId, List.of(parameterDTO),
                Status.ACTIVE, Instant.now(), true);
        return new DashboardClientDTO(null, "name", routingKey,
                Status.ACTIVE, List.of(feedConfigurationDTO), Instant.now(), EventPackage.BASIC);
    }
}
