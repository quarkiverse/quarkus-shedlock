package io.quarkiverse.shedlock.providers.mongo.reactive.deployment.deployment;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;

@ApplicationScoped
public class LockableResource {
    private final AtomicInteger callCounter = new AtomicInteger();

    @MongoReactiveSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    void doSomething() {
        callCounter.incrementAndGet();
    }

    public Integer getCallCount() {
        return callCounter.get();
    }

    public void reset() {
        callCounter.set(0);
    }
}
