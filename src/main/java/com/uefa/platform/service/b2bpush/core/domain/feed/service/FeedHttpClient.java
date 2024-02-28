package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import com.uefa.platform.service.b2bpush.core.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class FeedHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedHttpClient.class);

    private static final String X_API_CONSUMER_ID_HEADER_NAME = "x-api-consumer-id";

    private final RestTemplate restTemplate;

    private final String consumerHeader;

    public FeedHttpClient(RestTemplate restTemplate, @Value("${service.header.x-api-consumer-id}") String consumerHeader) {
        this.restTemplate = restTemplate;
        this.consumerHeader = consumerHeader;
    }

    @Cacheable(CacheConfiguration.FEED_HTTP_CLIENT_FEED_BY_URL)
    public String getFeedResult(final String URL) {
        try {
            //set consumer and content type headers
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_API_CONSUMER_ID_HEADER_NAME, consumerHeader);

            final HttpEntity<Object> entity = new HttpEntity<>(headers);

            final ResponseEntity<String> result = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            if (HttpStatus.OK.equals(result.getStatusCode())) {
                return result.getBody();
            } else {
                LOGGER.error("Unexpected status code returned for URL {}. Status code {}", URL, result.getStatusCode());
            }

        } catch (Exception e) {
            LOGGER.error("Exception while getting feed for URL {}, error {} !", URL, e);
            throw new RestClientException("Exception while getting feed for URL: " + URL + " with message: " + e.getMessage());
        }


        return null;
    }
}
