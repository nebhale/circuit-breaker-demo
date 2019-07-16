package io.pivotal.demo.server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class EndpointController {

    private static final Random DELAY = new Random(new SecureRandom().nextInt());

    private final Endpoint endpoint;

    private final AtomicBoolean status;

    EndpointController(Endpoint endpoint, Map<Endpoint, AtomicBoolean> status) {
        this.endpoint = endpoint;
        this.status = status.get(endpoint);
    }

    final Mono<ResponseEntity<String>> process() {
        ResponseEntity<String> response;

        if (this.status.get()) {
            response = ResponseEntity.ok().body(this.endpoint.toString().toLowerCase());
        } else {
            response = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return Mono
            .delay(Duration.ofMillis(DELAY.nextInt(100)))
            .thenReturn(response);
    }

    @RestController
    static final class AlphaEndpointController extends EndpointController {

        AlphaEndpointController(Map<Endpoint, AtomicBoolean> status) {
            super(Endpoint.ALPHA, status);
        }

        @GetMapping("/alpha")
        Mono<ResponseEntity<String>> alpha() {
            return process();
        }

    }

    @RestController
    static final class BravoEndpointController extends EndpointController {

        BravoEndpointController(Map<Endpoint, AtomicBoolean> status) {
            super(Endpoint.BRAVO, status);
        }

        @GetMapping("/bravo")
        Mono<ResponseEntity<String>> bravo() {
            return process();
        }

    }

    @RestController
    static final class CharlieEndpointController extends EndpointController {

        CharlieEndpointController(Map<Endpoint, AtomicBoolean> status) {
            super(Endpoint.CHARLIE, status);
        }

        @GetMapping("/charlie")
        Mono<ResponseEntity<String>> charlie() {
            return process();
        }

    }

}
