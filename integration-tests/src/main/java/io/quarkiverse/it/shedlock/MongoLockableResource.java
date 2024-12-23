package io.quarkiverse.it.shedlock;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import com.mongodb.client.MongoClient;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;

@ApplicationScoped
@Path("/mongoStorageLockableResource")
public class MongoLockableResource extends AbstractLockableResource {

    private final MongoClient defaultMongoClient;
    private final MongoClient clusterOneMongoClient;
    // must be declared to start mongodb testcontainers
    private final ReactiveMongoClient defaultReactiveMongoClient;
    private final ReactiveMongoClient clusterOneReactiveMongoClient;

    public MongoLockableResource(final MongoClient defaultMongoClient,
            @MongoClientName("cluster1") final MongoClient clusterOneMongoClient,
            final ReactiveMongoClient defaultReactiveMongoClient,
            @MongoClientName("cluster1") ReactiveMongoClient clusterOneReactiveMongoClient) {
        this.defaultMongoClient = Objects.requireNonNull(defaultMongoClient);
        this.clusterOneMongoClient = Objects.requireNonNull(clusterOneMongoClient);
        this.defaultReactiveMongoClient = Objects.requireNonNull(defaultReactiveMongoClient);
        this.clusterOneReactiveMongoClient = Objects.requireNonNull(clusterOneReactiveMongoClient);
    }

    @POST
    @Path("default")
    @MongoSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMongoClient() {
        doSomething();
    }

    @POST
    @Path("clusterOne")
    @MongoSchedulerLock(mongoClientName = "cluster1", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMongoClientClusterOne() {
        doSomething();
    }

    @POST
    @Path("reactive/default")
    @MongoReactiveSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingReactiveMongoClient() {
        doSomething();
    }

    @POST
    @Path("reactive/clusterOne")
    @MongoReactiveSchedulerLock(mongoClientName = "cluster1", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingReactiveMongoClientClusterOne() {
        doSomething();
    }

    @Override
    void resetLockStorage() {
        defaultMongoClient.getDatabase("shedLock").drop();
        clusterOneMongoClient.getDatabase("customDatabase").drop();
    }
}
