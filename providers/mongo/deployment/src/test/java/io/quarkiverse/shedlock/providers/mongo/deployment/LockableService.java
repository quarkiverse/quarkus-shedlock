package io.quarkiverse.shedlock.providers.mongo.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@ApplicationScoped
public class LockableService {
    @MongoSchedulerLock
    void execute() {
    }
}
