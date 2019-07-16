package io.pivotal.demo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class Server {

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args).start();
    }

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(Endpoint.values().length);
    }

    @Bean
    Map<Endpoint, AtomicBoolean> status() {
        Map<Endpoint, AtomicBoolean> status = new HashMap<>(Endpoint.values().length);

        Arrays.stream(Endpoint.values())
            .forEach(e -> status.put(e, new AtomicBoolean(true)));

        return status;
    }

}