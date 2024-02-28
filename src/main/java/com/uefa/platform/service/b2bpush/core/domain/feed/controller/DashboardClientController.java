package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.service.b2bpush.core.configuration.OpenApiConfiguration;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardClientDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.dto.DashboardIdAggregatorDTO;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.DashboardClientService;
import com.uefa.platform.service.b2bpush.core.domain.feed.service.IdAggregatorService;
import com.uefa.platform.web.handler.DisableHttpCache;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardClientController.URL_CLIENTS;
import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardFeedController.DASHBOARD_URL_PREFIX;


@RestController
@RequestMapping(path = DASHBOARD_URL_PREFIX + URL_CLIENTS)
@Hidden
@DisableHttpCache
@Tag(name = OpenApiConfiguration.FSP_B2B_PUSH_SERVICE_TAG_STRING)
public class DashboardClientController {
    public static final String URL_CLIENTS = "/clients";

    private final DashboardClientService clientService;

    private final IdAggregatorService idAggregatorService;

    public DashboardClientController(DashboardClientService clientService, IdAggregatorService idAggregatorService) {
        this.clientService = clientService;
        this.idAggregatorService = idAggregatorService;
    }

    @Operation(summary = "Returns client by id")
    @GetMapping("/{clientId}")
    public DashboardClientDTO getClientById(@PathVariable(value = "clientId") String clientId) {
        return clientService.getClientById(clientId);
    }

    @Operation(summary = "Returns all clients")
    @GetMapping
    public List<DashboardClientDTO> getAllClients() {
        return clientService.getAllClients();
    }

    @Operation(summary = "Create new client")
    @PostMapping
    public DashboardClientDTO createClient(@Valid @RequestBody DashboardClientDTO clientDTO) {
        return clientService.createClient(clientDTO);
    }

    @Operation(summary = "Update client by id")
    @PutMapping("/{clientId}")
    public DashboardClientDTO updateClient(@PathVariable(value = "clientId") String clientId,
                                           @Valid @RequestBody DashboardClientDTO clientDTO) {
        return clientService.updateClient(clientId, clientDTO);
    }

    @Operation(summary = "Change the client status to provided value")
    @PatchMapping("/{clientId}")
    public void changeStatus(@PathVariable(value = "clientId") String clientId,
                             @RequestParam(value = "status") Status status) {
        clientService.changeStatus(clientId, status);
    }

    @Operation(summary = "Delete client by id")
    @DeleteMapping("/{clientId}")
    public void deleteClient(@PathVariable(value = "clientId") String clientId) {
        clientService.deleteClient(clientId);
    }

    @Operation(summary = "Bootstrap feed configuration for a client feed configurationId")
    @PostMapping("/configuration/{configurationId}/bootstrap")
    public void bootstrap(@PathVariable(value = "configurationId") String configurationId,
                          @RequestParam(required = false) Map<String, String> bootstrapOptionalParameters) {
        clientService.bootstrap(configurationId, bootstrapOptionalParameters);
    }

    @Operation(summary = "Returns all ID Aggregators with parameters")
    @GetMapping("/aggregators")
    public List<DashboardIdAggregatorDTO> getAllIdAggregators() {
        return idAggregatorService.getAllIdAggregators();
    }
}
