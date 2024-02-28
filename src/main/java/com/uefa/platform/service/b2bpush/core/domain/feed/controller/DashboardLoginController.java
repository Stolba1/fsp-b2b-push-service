package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.service.b2bpush.core.configuration.OpenApiConfiguration;
import com.uefa.platform.web.handler.DisableHttpCache;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.uefa.platform.service.b2bpush.core.domain.feed.controller.DashboardLoginController.DASHBOARD_URL_PREFIX;

@RestController
@RequestMapping(path = DASHBOARD_URL_PREFIX)
@Hidden
@DisableHttpCache
@Tag(name = OpenApiConfiguration.FSP_B2B_PUSH_SERVICE_TAG_STRING)
public class DashboardLoginController {

    public static final String DASHBOARD_URL_PREFIX = "/v1/dashboard";

    public DashboardLoginController() {
        // Empty Constructor to avoid Multiple Beans Instantiation Problems
    }

    @Hidden
    @GetMapping(value = {"/login"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getAuth() {
        return "Login Successful";
    }
}
