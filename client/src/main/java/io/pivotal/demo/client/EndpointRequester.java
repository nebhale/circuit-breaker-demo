package io.pivotal.demo.client;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

abstract class EndpointRequester {

    private final String endpoint;

    private final RestTemplate restTemplate;

    EndpointRequester(String endpoint, RestTemplateBuilder restTemplateBuilder, String rootUri) {
        this.endpoint = endpoint;
        this.restTemplate = restTemplateBuilder.rootUri(rootUri).build();
    }

    final String fallback() {
        return "fallback";
    }

    final String process() {
        return this.restTemplate.getForObject(this.endpoint, String.class);
    }

    @Component
    static class AlphaEndpointRequester extends EndpointRequester {

        AlphaEndpointRequester(RestTemplateBuilder restTemplateBuilder, @Value("${root.uri}") String rootUri) {
            super("/alpha", restTemplateBuilder, rootUri);
        }

        @HystrixCommand(fallbackMethod = "fallback")
        String alpha() {
            return process();
        }
    }

    @Component
    static class BravoEndpointRequester extends EndpointRequester {

        BravoEndpointRequester(RestTemplateBuilder restTemplateBuilder, @Value("${root.uri}") String rootUri) {
            super("/bravo", restTemplateBuilder, rootUri);
        }

        @HystrixCommand(fallbackMethod = "fallback")
        String bravo() {
            return process();
        }
    }

    @Component
    static class CharlieEndpointRequester extends EndpointRequester {

        CharlieEndpointRequester(RestTemplateBuilder restTemplateBuilder, @Value("${root.uri}") String rootUri) {
            super("/charlie", restTemplateBuilder, rootUri);
        }

        @HystrixCommand(fallbackMethod = "fallback")
        String charlie() {
            return process();
        }
    }
}
