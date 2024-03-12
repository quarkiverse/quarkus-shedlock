package io.quarkiverse.shedlock.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;

@Path("/shedlock")
@ApplicationScoped
public class SchedulerLockResource {
    @GET
    @InMemorySchedulerLock
    public void runUsingLock() {
    }
}
