package io.quarkiverse.it.shedlock;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public abstract class AbstractLockableResource {

    private final AtomicInteger callCounter;

    public AbstractLockableResource() {
        this.callCounter = new AtomicInteger();
    }

    protected final int doSomething() {
        return callCounter.incrementAndGet();
    }

    @POST
    @Path("reset")
    public final void reset() {
        callCounter.set(0);
        resetLockStorage();
    }

    @GET
    @Path("callCount")
    public final Integer getCallCount() {
        return callCounter.get();
    }

    abstract void resetLockStorage();
}
