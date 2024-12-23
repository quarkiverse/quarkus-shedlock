package io.quarkiverse.shedlock.providers.mongo.deployment;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@ApplicationScoped
public class LockableResource {
    private final AtomicInteger callCounter = new AtomicInteger();

    @MongoSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
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
