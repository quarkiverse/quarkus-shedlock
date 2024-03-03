package io.quarkiverse.shedlock.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@Path("/shedlock")
@ApplicationScoped
public class SchedulerLockResource {
    @GET
    @MongoSchedulerLock
    public void runUsingLock() {
    }

    @GET
    @Path("/clusterOne")
    @MongoSchedulerLock(mongoClientName = "cluster1")
    public void runUsingLockOnClusterOne() {
    }
}
