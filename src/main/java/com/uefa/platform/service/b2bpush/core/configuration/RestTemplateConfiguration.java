package com.uefa.platform.service.b2bpush.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // set timeouts
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(20000);
        return new RestTemplate(requestFactory);
    }
}
