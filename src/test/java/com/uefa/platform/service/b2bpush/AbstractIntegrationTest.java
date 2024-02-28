package com.uefa.platform.service.b2bpush;

import com.uefa.platform.test.ActiveProfileOverrideResolver;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.ResponseSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.MalformedURLException;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;


@ActiveProfiles(profiles = {Application.Profiles.TEST}, resolver = ActiveProfileOverrideResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Value("${dashboardCredentials.username}")
    private String dashboardUser;

    @Value("${dashboardCredentials.testPassword}")
    private String dashboardPassword;

    @Value("${dataExplorerCredentials.username}")
    private String dataExplorerUser;

    @Value("${dataExplorerCredentials.testPassword}")
    private String dataExplorerPassword;

    protected ValidatableResponse get(String path, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .when().get(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse post(String path, Object body, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse put(String path, Object body, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().put(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse post(String path, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .when().post(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse patch(String path, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .when().patch(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse patch(String path, Object body, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().patch(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse delete(String path, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dashboardUser, dashboardPassword)
                    .baseUri(url.toString())
                    .when().delete(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse postNoAuth(String path, Object body, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .baseUri(url.toString())
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when().post(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse getForDataExplorer(String path, ResponseSpecification responseSpec) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .auth().basic(dataExplorerUser, dataExplorerPassword)
                    .baseUri(url.toString())
                    .when().get(path)
                    .then().spec(responseSpec);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected static ResponseSpecification responseOk() {
        return new ResponseSpecBuilder()
                .expectBody(not(emptyOrNullString()))
                .expectContentType(ContentType.JSON)
                .expectStatusCode(HttpStatus.OK.value())
                .build();

    }

    protected static ResponseSpecification withResponse(HttpStatus httpStatus) {
        return new ResponseSpecBuilder()
                .expectBody(not(emptyOrNullString()))
                .expectContentType(ContentType.JSON)
                .expectStatusCode(httpStatus.value())
                .build();

    }

    protected ValidatableResponse givenLoginPathIsOk(String path, String userName, String password) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .baseUri(url.toString())
                    .auth().basic(userName, password)
                    .when().get(path)
                    .then()
                    .spec(new ResponseSpecBuilder()
                            .expectStatusCode(HttpStatus.OK.value())
                            .build());
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    protected ValidatableResponse givenLoginPathIsUnathorized(String path, String userName, String password) {
        try {
            final URL url = new URL("http://localhost:" + port + "/");
            return given().filter(new RequestLoggingFilter()).filter(new ResponseLoggingFilter())
                    .baseUri(url.toString())
                    .auth().basic(userName, password)
                    .when().get(path)
                    .then()
                    .spec(new ResponseSpecBuilder()
                            .expectStatusCode(HttpStatus.UNAUTHORIZED.value())
                            .build());
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }
}
