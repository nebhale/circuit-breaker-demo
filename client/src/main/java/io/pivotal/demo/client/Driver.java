package io.pivotal.demo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

abstract class Driver implements Callable<Void>, Lifecycle {

    private static final Duration LOG_INTERVAL = Duration.ofSeconds(5);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String endpoint;

    private final ExecutorService executor;

    private Future<Void> future;

    Driver(String endpoint, ExecutorService executor) {
        this.endpoint = endpoint;
        this.executor = executor;
    }

    @Override
    public final Void call() {
        Instant next = nextLog();

        for (; ; ) {
            String s = request();

            if (Instant.now().isAfter(next)) {
                this.logger.info("{}: {}", String.format("%1$-8s", this.endpoint), s);
                next = nextLog();
            }
        }
    }

    @Override
    public final boolean isRunning() {
        return this.future != null && !this.future.isDone();
    }

    @Override
    public final void start() {
        this.future = this.executor.submit(this);
    }

    @Override
    public final void stop() {
        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    abstract String request();

    private Instant nextLog() {
        return Instant.now().plus(LOG_INTERVAL);
    }

    @Component
    static final class AlphaDriver extends Driver {

        private final EndpointRequester.AlphaEndpointRequester requester;

        AlphaDriver(EndpointRequester.AlphaEndpointRequester requester, ExecutorService executor) {
            super("/alpha", executor);
            this.requester = requester;
        }

        @Override
        String request() {
            return this.requester.alpha();
        }
    }

    @Component
    static final class BravoDriver extends Driver {

        private final EndpointRequester.BravoEndpointRequester requester;

        BravoDriver(EndpointRequester.BravoEndpointRequester requester, ExecutorService executor) {
            super("/bravo", executor);
            this.requester = requester;
        }

        @Override
        String request() {
            return this.requester.bravo();
        }
    }

    @Component
    static final class CharlieDriver extends Driver {

        private final EndpointRequester.CharlieEndpointRequester requester;

        CharlieDriver(EndpointRequester.CharlieEndpointRequester requester, ExecutorService executor) {
            super("/charlie", executor);
            this.requester = requester;
        }

        @Override
        String request() {
            return this.requester.charlie();
        }
    }
}
