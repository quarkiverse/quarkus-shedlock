package io.quarkiverse.shedlock.providers.jdbc.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;

@ApplicationScoped
public class LockableServiceUsingUnknownDataSource {
    @JdbcSchedulerLock(dataSourceName = "unknownDataSource")
    void execute() {
    }
}
