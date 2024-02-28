package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice;
import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice.ClientWithNameAlreadyExistsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.FeedConfigurationDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.ParameterDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Feed;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessingSource;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.ACTIVE;
import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.INACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardClientServiceTest {

    @InjectMocks
    private DashboardClientService dashboardClientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ConfigurationProcessorService configurationProcessorService;

    @Mock
    private FeedProcessorService feedProcessorService;

    @Test
    void testCreateClient() {
        String feedId = "123";
        String routingKey = "routing key";
        ParameterDTO parameterDTO = new ParameterDTO("name param", "value param", null, null);
        FeedConfigurationDTO feedConfigurationDTO = new FeedConfigurationDTO(null, feedId, List.of(parameterDTO),
                ACTIVE, null, true);
        DashboardClientDTO clientDTO = new DashboardClientDTO(null, "name", routingKey,
                ACTIVE, List.of(feedConfigurationDTO), null, EventPackage.EXTENDED);

        Client clientDb = Client.instanceOf(clientDTO);
        when(feedRepository.existsById(feedId))
                .thenReturn(true);
        when(clientRepository.save(any()))
                .thenReturn(clientDb);
        DashboardClientDTO client = dashboardClientService.createClient(clientDTO);
        Assertions.assertEquals(clientDTO.getName(), client.getName());
        Assertions.assertEquals(clientDTO.getStatus(), client.getStatus());
        Assertions.assertEquals(clientDTO.getRoutingKey(), client.getRoutingKey());
        Assertions.assertNotNull(client.getConfigurations().get(0).getId());
        Assertions.assertEquals(clientDTO.getConfigurations().get(0).getFeedId(), client.getConfigurations().get(0).getFeedId());
        Assertions.assertEquals(clientDTO.getConfigurations().get(0).getStatus(), client.getConfigurations().get(0).getStatus());
        Assertions.assertNull(client.getConfigurations().get(0).getLastSentTime());
        Assertions.assertEquals(clientDTO.getLastUpdateTime(), client.getLastUpdateTime());
        Assertions.assertEquals(clientDTO.getEventPackage(), client.getEventPackage());
    }

    @Test
    void testCreateClientAlreadyExists() {
        String routingKey = "routing key";
        DashboardClientDTO clientDTO = new DashboardClientDTO(null, "name", routingKey,
                ACTIVE, List.of(), Instant.now(), EventPackage.BASIC);

        when(clientRepository.existsByRoutingKey(routingKey))
                .thenReturn(true);

        ControllerAdvice.ClientWithRoutingKeyAlreadyExistsException thrown = Assertions.assertThrows(
                ControllerAdvice.ClientWithRoutingKeyAlreadyExistsException.class,
                () -> dashboardClientService.createClient(clientDTO)
        );
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("Client with routingKey: %s found in repository", clientDTO.getRoutingKey())));
    }

    @Test
    void testGetClientById() {
        String clientId = "456";
        Client client = new Client(clientId, "name", "routingKey",
                List.of(), ACTIVE, Instant.now(), EventPackage.BASIC);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));
        DashboardClientDTO clientResponse = dashboardClientService.getClientById(clientId);
        Assertions.assertEquals(client.getName(), clientResponse.getName());
        Assertions.assertEquals(client.getStatus(), clientResponse.getStatus());
        Assertions.assertEquals(client.getRoutingKey(), clientResponse.getRoutingKey());
        Assertions.assertEquals(client.getLastUpdateTime(), clientResponse.getLastUpdateTime());
        Assertions.assertEquals(client.getEventPackage(), clientResponse.getEventPackage());
    }

    @Test
    void testGetClientByIdNotExists() {
        String clientId = "456";
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> dashboardClientService.getClientById(clientId));
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("No client found for clientId:%s",
                        clientId)));
    }

    @Test
    void testGetClients() {
        String clientId = "456";
        Client client = new Client(clientId, "name", "routingKey",
                List.of(), ACTIVE, Instant.now(), EventPackage.EXTENDED);

        when(clientRepository.findAll())
                .thenReturn(List.of(client));
        List<DashboardClientDTO> clientsResponse = dashboardClientService.getAllClients();
        Assertions.assertEquals(1, clientsResponse.size());
        Assertions.assertEquals(client.getName(), clientsResponse.get(0).getName());
        Assertions.assertEquals(client.getStatus(), clientsResponse.get(0).getStatus());
        Assertions.assertEquals(client.getRoutingKey(), clientsResponse.get(0).getRoutingKey());
        Assertions.assertEquals(client.getLastUpdateTime(), clientsResponse.get(0).getLastUpdateTime());
        Assertions.assertEquals(client.getEventPackage(), clientsResponse.get(0).getEventPackage());
    }

    @Test
    void testGetEmptyListForClients() {
        when(clientRepository.findAll()).thenReturn(List.of());
        List<DashboardClientDTO> clients = dashboardClientService.getAllClients();
        Assertions.assertEquals(0, clients.size());
    }

    @Test
    void testUpdateClient() {
        String clientId = "456";
        String routingKey = "routing key";
        DashboardClientDTO clientDTO = new DashboardClientDTO(clientId, "name", routingKey,
                ACTIVE, List.of(), Instant.now(), EventPackage.BASIC);
        Client client = Client.instanceOf(clientDTO);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.upsert(any(), eq(clientId))).thenReturn(client);
        when(clientRepository.existsByRoutingKeyAndIdNot(routingKey, clientId))
                .thenReturn(false);

        DashboardClientDTO result = dashboardClientService.updateClient(clientId, clientDTO);
        Assertions.assertEquals(clientDTO.getName(), result.getName());
        Assertions.assertEquals(clientDTO.getStatus(), result.getStatus());
        Assertions.assertEquals(clientDTO.getRoutingKey(), result.getRoutingKey());
        Assertions.assertEquals(clientDTO.getConfigurations(), result.getConfigurations());
        Assertions.assertEquals(clientDTO.getLastUpdateTime(), result.getLastUpdateTime());
        Assertions.assertEquals(clientDTO.getEventPackage(), result.getEventPackage());
    }

    @Test
    void testUpdateClientAlreadyExists() {
        String clientId = "456";
        String routingKey = "routing key";
        DashboardClientDTO clientDTO = new DashboardClientDTO(clientId, "name", routingKey,
                ACTIVE, List.of(), Instant.now(), EventPackage.BASIC);
        Client client = Client.instanceOf(clientDTO);
        when(clientRepository.existsByRoutingKeyAndIdNot(routingKey, clientId))
                .thenReturn(true);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ControllerAdvice.ClientWithRoutingKeyAlreadyExistsException thrown = Assertions.assertThrows(
                ControllerAdvice.ClientWithRoutingKeyAlreadyExistsException.class,
                () -> dashboardClientService.updateClient(clientId, clientDTO)
        );
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("Client with routingKey: %s found in repository", clientDTO.getRoutingKey())));
    }

    @Test
    void testChangeClientStatus() {
        String clientId = "1";
        doNothing().when(clientRepository).changeStatus(clientId, INACTIVE);
        Client existingClient = new Client(clientId, "name", "routingKey",
                List.of(), ACTIVE, Instant.now(), EventPackage.EXTENDED);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        dashboardClientService.changeStatus(clientId, INACTIVE);
        verify(clientRepository).changeStatus(clientId, INACTIVE);
    }

    @Test
    void testDeleteById() {
        String clientId = "1";
        doNothing().when(clientRepository).deleteById(clientId);
        Client existingClient = new Client(clientId, "name", "routingKey",
                List.of(), ACTIVE, Instant.now(), EventPackage.BASIC);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        dashboardClientService.deleteClient(clientId);
        verify(clientRepository).deleteById(clientId);
    }

    @Test
    void testDeleteByIdWhenClientNotExists() {
        String clientId = "1";
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> dashboardClientService.deleteClient(clientId));
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("No client found for clientId:%s",
                        clientId)));
    }

    @Test
    void testBootstrap() {
        final Feed feedForProcessing =
                new Feed("id1", "FEED_1_CODE", Status.ACTIVE, "https://comp-int.uefa.com/v1/teams?competitionId={competitionId}&seasonYear={seasonYear}",
                        Set.of("competitionId", "seasonYear"), null, Instant.now().minus(10, ChronoUnit.MINUTES), 8, null, null, 3);

        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "id1", List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);

        when(feedRepository.findByIdAndStatus(feedForProcessing.getId(), ACTIVE))
                .thenReturn(Optional.of(feedForProcessing));
        when(clientRepository.findAllActiveClientsByConfigurationId(configuration1.getId()))
                .thenReturn(List.of(client));

        dashboardClientService.bootstrap(configuration1.getId(), null);

        verify(feedProcessorService, times(1))
                .processStaticFeedsConfigurations(List.of(feedForProcessing), List.of(client), "configId1", ProcessingSource.BOOTSTRAP);
    }

    @Test
    void testBootstrapWhenClientNotFoundByConfigurationId() {
        String configurationId = "123";
        when(clientRepository.findAllActiveClientsByConfigurationId(configurationId))
                .thenReturn(List.of());

        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> dashboardClientService.bootstrap(configurationId, null));
        Assertions.assertTrue(thrown.getMessage()
                .contains(String.format("No client found with configurationId: %s",
                        configurationId)));

        verifyNoInteractions(configurationProcessorService);
    }

    @Test
    void testBootstrapWhenFeedNotFound() {
        String feedId = "id1";
        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration.Parameter parameter2 = new Client.FeedConfiguration.Parameter("seasonYear", "2022", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", feedId, List.of(parameter1, parameter2),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);
        Client client = new Client("id1", "Broadcaster", "TEST_KEY", List.of(configuration1), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);

        when(feedRepository.findByIdAndStatus(feedId, ACTIVE))
                .thenReturn(Optional.empty());
        when(clientRepository.findAllActiveClientsByConfigurationId(configuration1.getId()))
                .thenReturn(List.of(client));

        dashboardClientService.bootstrap(configuration1.getId(), null);

        verifyNoInteractions(configurationProcessorService);
    }

    @DisplayName("Testing trying to save Client with an existing name")
    @Test
    void dtoWithExistingClientName_ShouldThrowException() {
        DashboardClientDTO clientDto =
                new DashboardClientDTO("id-123", "existing-name", "routingKey123",
                        ACTIVE, Collections.emptyList(), Instant.now(), EventPackage.BASIC);

        when(clientRepository.existsByName(anyString())).thenReturn(true);

        ClientWithNameAlreadyExistsException exception = Assertions.assertThrows(
                ClientWithNameAlreadyExistsException.class,
                () -> dashboardClientService.createClient(clientDto)
        );

        Assertions.assertEquals("Client with name: 'existing-name' already found in repository",
                exception.getMessage());
    }
}