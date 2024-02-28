package com.uefa.platform.service.b2bpush.core.domain.feed.controller;

import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

public class DashboardLoginControllerTest extends AbstractIntegrationTest {

    private static final String PATH = "/v1/dashboard";

    @Value("${dashboardCredentials.username}")
    private String dashboardUser;

    @Value("${dashboardCredentials.testPassword}")
    private String dashboardTestPassword;

    @Test
    public void getAuth() {
        String path = PATH + "/login";
        givenLoginPathIsOk(path, dashboardUser, dashboardTestPassword)
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void getAuthUnauthorized() {
        String path = PATH + "/login";
        givenLoginPathIsUnathorized(path, dashboardUser, "12345").statusCode(HttpStatus.UNAUTHORIZED.value());
    }
    
}
