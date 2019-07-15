package io.pivotal.demo.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.CircuitBreakerMetrics;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.circuitbreaker.commons.CircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.CircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class Client {

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }

    @Bean
    CircuitBreaker circuitBreaker(CircuitBreakerFactory circuitBreakerFactory, CircuitBreakerRegistry registry, MeterRegistry meterRegistry) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("alpha");
        registry.circuitBreaker("alpha", CircuitBreakerConfig.custom()
            .waitDurationInOpenState(Duration.ofSeconds(1))
            .build());

        CircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);

        return circuitBreaker;
    }

    @Bean
    Resilience4JCircuitBreakerFactory circuitBreakerFactory(CircuitBreakerRegistry registry) {
        Resilience4JCircuitBreakerFactory factory = new Resilience4JCircuitBreakerFactory();

        factory.configureCircuitBreakerRegistry(registry);
        factory.configureDefault(s -> new Resilience4JConfigBuilder(s)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .build())
            .timeLimiterConfig(TimeLimiterConfig.ofDefaults())
            .build());

        return factory;
    }

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Component
    static final class Reader implements ApplicationRunner {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CircuitBreaker circuitBreaker;

        private final RestTemplate restTemplate;

        Reader(CircuitBreaker circuitBreaker, RestTemplateBuilder restTemplateBuilder, @Value("${endpoint}") String uri) {
            this.circuitBreaker = circuitBreaker;
            this.restTemplate = restTemplateBuilder.rootUri(uri).build();

            this.logger.info("Connecting to {}", uri);
        }

        @Override
        public void run(ApplicationArguments args) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String s = this.circuitBreaker.run(
                    () -> {
                        this.logger.debug("Attempting");
                        return this.restTemplate.getForObject("/", String.class);
                    },
                    t -> "fallback");

//                if (i % 10_000 == 0) {
                    this.logger.debug("Call {}: {}", i, s);
//                }
            }
        }
    }

}
