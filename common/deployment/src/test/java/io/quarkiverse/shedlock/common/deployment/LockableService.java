package io.quarkiverse.shedlock.common.deployment;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LockableService {
    @TestSchedulerLock
    void execute() {
    }

    @TestSchedulerLock
    Integer unsupportedReturn() {
        return 0;
    }
}
