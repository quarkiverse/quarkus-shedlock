package io.quarkiverse.shedlock.common.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import net.javacrumbs.shedlock.cdi.SchedulerLock;

@ApplicationScoped
public class LockableService {
    @SchedulerLock(name = "lockable")
    void execute() {
    }
}
