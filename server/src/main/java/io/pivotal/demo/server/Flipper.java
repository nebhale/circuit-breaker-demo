package io.pivotal.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class Flipper implements Callable<Void>, Lifecycle {

    private static final Random DELAY = new Random(new SecureRandom().nextInt());

    private static final int MAX_DELAY_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Endpoint endpoint;

    private final ExecutorService executor;

    private final AtomicBoolean status;

    private Future<Void> future;

    Flipper(Endpoint endpoint, Map<Endpoint, AtomicBoolean> status, ExecutorService executor) {
        this.endpoint = endpoint;
        this.executor = executor;
        this.status = status.get(endpoint);
    }

    @Override
    public final Void call() throws InterruptedException {
        for (; ; ) {
            Thread.sleep(Duration.ofSeconds(DELAY.nextInt(MAX_DELAY_SECONDS)).toMillis());

            if (invert()) {
                this.logger.info("Enabling {} endpoint", this.endpoint);
            } else {
                this.logger.info("Disabling {} endpoint", this.endpoint);
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

    private boolean invert() {
        boolean current;
        do {
            current = this.status.get();
        } while (!this.status.compareAndSet(current, !current));

        return !current;
    }

    @Component
    static final class AlphaFlipper extends Flipper {

        AlphaFlipper(Map<Endpoint, AtomicBoolean> status, ExecutorService executor) {
            super(Endpoint.ALPHA, status, executor);
        }
    }

    @Component
    static final class BravoFlipper extends Flipper {

        BravoFlipper(Map<Endpoint, AtomicBoolean> status, ExecutorService executor) {
            super(Endpoint.BRAVO, status, executor);
        }
    }

    @Component
    static final class CharlieFlipper extends Flipper {

        CharlieFlipper(Map<Endpoint, AtomicBoolean> status, ExecutorService executor) {
            super(Endpoint.CHARLIE, status, executor);
        }
    }
}
