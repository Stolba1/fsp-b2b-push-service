package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedHttpClientTest {

    @Mock
    private RestTemplate restTemplate;

    private FeedHttpClient client;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        client = new FeedHttpClient(restTemplate, "x-api-header");
    }

    @Test
    void testGetResult() {
        when(restTemplate.exchange(eq("URL"), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity("{}", HttpStatus.OK));
        String result = client.getFeedResult("URL");

        Assertions.assertEquals("{}", result);
        verify(restTemplate, times(1))
                .exchange(eq("URL"), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    void testGetResultDifferentStatusCode() {
        when(restTemplate.exchange(any(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity("{}", HttpStatus.NOT_FOUND));

        Assertions.assertThrows(RestClientException.class,() -> client.getFeedResult("URL1"));
        verify(restTemplate, times(1))
                .exchange(eq("URL1"), eq(HttpMethod.GET), any(), eq(String.class));
    }
}
