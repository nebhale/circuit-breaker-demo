package io.pivotal.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Random;

@SpringBootApplication
public class Server {

    private boolean enabled = true;

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @RestController
    final class Controller {

        @PostConstruct
        void flipper() {
        }

        @GetMapping("/")
        Mono<ResponseEntity<String>> root() {
            if (enabled) {
                return Mono.just(ResponseEntity.ok().body("Hello World"));
            } else {
                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
            }
        }
    }

    @Component
    final class Flipper implements ApplicationRunner {

        private static final int MAX_DELAY = 10;

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Random random = new Random();

        @Override
        public void run(ApplicationArguments args) {
            Flux.range(0, Integer.MAX_VALUE)
                .delayUntil(i -> Mono.delay(Duration.ofSeconds(this.random.nextInt(MAX_DELAY))))
                .doOnNext(i -> {
                    if (enabled = !enabled) {
                        this.logger.debug("Enabling endpoint");
                    } else {
                        this.logger.debug("Disabling endpoint");
                    }
                })
                .subscribe();
        }
    }

}
