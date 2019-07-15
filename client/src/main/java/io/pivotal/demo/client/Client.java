package io.pivotal.demo.client;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@EnableCircuitBreaker
@SpringBootApplication
public class Client {

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }

    @Component
    static final class Reader implements ApplicationRunner {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Remote remote;

        Reader(Remote remote) {
            this.remote = remote;
        }

        @Override
        public void run(ApplicationArguments args) {
            Instant nextLog = nextLog();

            for (int i = 0; i < Integer.MAX_VALUE; i++) {

                String s = this.remote.request();

                if (Instant.now().isAfter(nextLog)) {
                    this.logger.debug("Call {}: {}", i, s);
                    nextLog = nextLog();
                }
            }
        }

        private Instant nextLog() {
            return Instant.now().plus(Duration.ofSeconds(2));
        }
    }

    @Component
    static class Remote {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final RestTemplate restTemplate;

        Remote(RestTemplateBuilder restTemplateBuilder, @Value("${endpoint}") String uri) {
            this.restTemplate = restTemplateBuilder.rootUri(uri).build();
            this.logger.info("Connecting to {}", uri);
        }

        String fallback() {
            return "fallback";
        }

        @HystrixCommand(fallbackMethod = "fallback")
        String request() {
            return this.restTemplate.getForObject("/", String.class);
        }
    }

}
