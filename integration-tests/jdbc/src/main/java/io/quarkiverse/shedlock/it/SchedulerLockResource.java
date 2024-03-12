package io.quarkiverse.shedlock.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;

@Path("/shedlock")
@ApplicationScoped
public class SchedulerLockResource {
    @GET
    @JdbcSchedulerLock
    public void runUsingLock() {
    }

    @GET
    @Path("/master")
    @JdbcSchedulerLock(dataSourceName = "master")
    public void runUsingLockOnMaster() {
    }
}
