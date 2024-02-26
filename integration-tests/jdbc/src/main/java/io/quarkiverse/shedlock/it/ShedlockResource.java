package io.quarkiverse.shedlock.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import net.javacrumbs.shedlock.cdi.SchedulerLock;

@Path("/shedlock")
@ApplicationScoped
public class ShedlockResource {
    @GET
    @SchedulerLock(name = "lockable")
    public void runUsingLock() {
    }
}
