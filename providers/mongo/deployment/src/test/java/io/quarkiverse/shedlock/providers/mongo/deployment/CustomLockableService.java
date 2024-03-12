package io.quarkiverse.shedlock.providers.mongo.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@ApplicationScoped
public class CustomLockableService {

    @MongoSchedulerLock(mongoClientName = "cluster1")
    void execute() {
    }
}
