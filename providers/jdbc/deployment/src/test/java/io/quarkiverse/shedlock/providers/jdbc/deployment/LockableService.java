package io.quarkiverse.shedlock.providers.jdbc.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;

@ApplicationScoped
public class LockableService {
    @JdbcSchedulerLock
    void execute() {
    }
}
