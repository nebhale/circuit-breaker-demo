package io.pivotal.demo.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableCircuitBreaker
@SpringBootApplication
public class Client {

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args).start();
    }

    @Bean
    ExecutorService executor() {
        return Executors.newFixedThreadPool(3);
    }

}
