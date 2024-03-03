package io.quarkiverse.shedlock.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@Path("/shedlock")
@ApplicationScoped
public class SchedulerLockResource {
    @GET
    @Path("/in-memory")
    @InMemorySchedulerLock
    public void runUsingInMemoryLock() {
    }

    @GET
    @Path("/jdbc")
    @JdbcSchedulerLock
    public void runUsingJdbcLock() {
    }

    @GET
    @Path("/mongo")
    @MongoSchedulerLock
    public void runUsingMongoLock() {
    }
}
