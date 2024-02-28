package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice.ClientWithNameAlreadyExistsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.controller.ControllerAdvice.ClientWithRoutingKeyAlreadyExistsException;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.FeedType;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.ProcessingSource;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.ClientRepository;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.repository.FeedRepository;
import com.uefa.platform.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status.ACTIVE;

@Service
public class DashboardClientService {
    private static final String CLIENT_NOT_FOUND_EXCEPTION = "No client found for clientId:%s";
    private static final String CLIENT_BY_CONFIGURATION_ID_NOT_FOUND_EXCEPTION = "No client found with configurationId: %s";
    private static final String FEED_NOT_FOUND_EXCEPTION = "No feed found for feedId: %s";
    private final ClientRepository clientRepository;
    private final FeedRepository feedRepository;
    private final ConfigurationProcessorService configurationProcessorService;
    private final FeedProcessorService feedProcessorService;

    public DashboardClientService(ClientRepository clientRepository, FeedRepository feedRepository,
                                  ConfigurationProcessorService configurationProcessorService, FeedProcessorService feedProcessorService) {
        this.clientRepository = clientRepository;
        this.feedRepository = feedRepository;
        this.configurationProcessorService = configurationProcessorService;
        this.feedProcessorService = feedProcessorService;
    }

    public DashboardClientDTO createClient(DashboardClientDTO clientDTO) {
        if (clientRepository.existsByRoutingKey(clientDTO.getRoutingKey())) {
            throw new ClientWithRoutingKeyAlreadyExistsException(clientDTO.getRoutingKey());
        }
        if (clientRepository.existsByName(clientDTO.getName())) {
            throw new ClientWithNameAlreadyExistsException(clientDTO.getName());
        }

        checkIfFeedsExists(clientDTO);

        final Client savedClient = clientRepository.save(Client.instanceOf(clientDTO));
        return DashboardClientDTO.instanceOf(savedClient);
    }

    public DashboardClientDTO getClientById(final String clientId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND_EXCEPTION, clientId)));
        return DashboardClientDTO.instanceOf(client);
    }

    public List<DashboardClientDTO> getAllClients() {
        List<Client> clients = clientRepository.findAll();

        return clients.stream()
                .map(DashboardClientDTO::instanceOf)
                .sorted(Comparator.comparing(DashboardClientDTO::getName))
                .collect(Collectors.toList());
    }

    public DashboardClientDTO updateClient(String clientId, DashboardClientDTO clientDTO) {
        Optional<Client> existingClient = clientRepository.findById(clientId);
        if (existingClient.isEmpty()) {
            throw new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND_EXCEPTION, clientId));
        }

        if (clientRepository.existsByRoutingKeyAndIdNot(clientDTO.getRoutingKey(), clientId)) {
            throw new ClientWithRoutingKeyAlreadyExistsException(clientDTO.getRoutingKey());
        }

        checkIfFeedsExists(clientDTO);

        final Client savedClient = clientRepository.upsert(Client.ClientUpdateBuilder
                .create()
                .with(Client.instanceOf(clientDTO), existingClient.get().getConfigurations())
                .build(), clientId);

        return DashboardClientDTO.instanceOf(savedClient);
    }


    public void changeStatus(String clientId, Status status) {
        Optional<Client> existingClient = clientRepository.findById(clientId);
        if (existingClient.isEmpty()) {
            throw new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND_EXCEPTION, clientId));
        }
        clientRepository.changeStatus(clientId, status);
    }

    public void deleteClient(String clientId) {
        Optional<Client> existingClient = clientRepository.findById(clientId);
        if (existingClient.isEmpty()) {
            throw new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND_EXCEPTION, clientId));
        }
        clientRepository.deleteById(clientId);
    }

    public void bootstrap(String configurationId, Map<String, String> bootstrapOptionalParameters) {
        Client activeClient = clientRepository.findAllActiveClientsByConfigurationId(configurationId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_BY_CONFIGURATION_ID_NOT_FOUND_EXCEPTION, configurationId)));

        activeClient.getConfigurations()
                .stream()
                .filter(config -> config.getId().equals(configurationId) && ACTIVE.equals(config.getStatus()))
                .findFirst()
                .ifPresent(config -> feedRepository.findByIdAndStatus(config.getFeedId(), ACTIVE)
                        .ifPresent(feed -> {
                            if (FeedType.LIVE.equals(feed.getType())) {
                                configurationProcessorService.processLiveFeed(feed, config, bootstrapOptionalParameters);
                            } else {
                                feedProcessorService.processStaticFeedsConfigurations(List.of(feed), List.of(activeClient), configurationId,
                                        ProcessingSource.BOOTSTRAP);
                            }
                        }));
    }


    private void checkIfFeedsExists(DashboardClientDTO clientDTO) {
        clientDTO.getConfigurations()
                .forEach(feedConfigurationDTO -> {
                    if (!feedRepository.existsById(feedConfigurationDTO.getFeedId())) {
                        throw new ResourceNotFoundException(String.format(FEED_NOT_FOUND_EXCEPTION, feedConfigurationDTO.getFeedId()));
                    }
                });
    }
}
