package io.quarkiverse.it.shedlock;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import com.mongodb.client.MongoClient;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLockExecutor;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;

@ApplicationScoped
@Path("/mongoStorageLockableResource")
public class MongoLockableResource extends AbstractLockableResource {

    private final MongoClient defaultMongoClient;
    private final MongoClient clusterOneMongoClient;
    // must be declared to start mongodb testcontainers
    private final ReactiveMongoClient defaultReactiveMongoClient;
    private final ReactiveMongoClient clusterOneReactiveMongoClient;
    private final SchedulerLockExecutor defaultSchedulerLockExecutor;
    private final SchedulerLockExecutor clusterOneSchedulerLockExecutor;
    private final SchedulerLockExecutor defaultReactiveSchedulerLockExecutor;
    private final SchedulerLockExecutor clusterOneReactiveSchedulerLockExecutor;

    public MongoLockableResource(final MongoClient defaultMongoClient,
            @MongoClientName("cluster1") final MongoClient clusterOneMongoClient,
            final ReactiveMongoClient defaultReactiveMongoClient,
            @MongoClientName("cluster1") ReactiveMongoClient clusterOneReactiveMongoClient,
            @MongoSchedulerLockExecutor(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor defaultSchedulerLockExecutor,
            @MongoSchedulerLockExecutor(mongoClientName = "cluster1", lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor clusterOneSchedulerLockExecutor,
            @MongoReactiveSchedulerLockExecutor(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor defaultReactiveSchedulerLockExecutor,
            @MongoReactiveSchedulerLockExecutor(mongoClientName = "cluster1", lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor clusterOneReactiveSchedulerLockExecutor) {
        this.defaultMongoClient = Objects.requireNonNull(defaultMongoClient);
        this.clusterOneMongoClient = Objects.requireNonNull(clusterOneMongoClient);
        this.defaultReactiveMongoClient = Objects.requireNonNull(defaultReactiveMongoClient);
        this.clusterOneReactiveMongoClient = Objects.requireNonNull(clusterOneReactiveMongoClient);
        this.defaultSchedulerLockExecutor = Objects.requireNonNull(defaultSchedulerLockExecutor);
        this.clusterOneSchedulerLockExecutor = Objects.requireNonNull(clusterOneSchedulerLockExecutor);
        this.defaultReactiveSchedulerLockExecutor = Objects.requireNonNull(defaultReactiveSchedulerLockExecutor);
        this.clusterOneReactiveSchedulerLockExecutor = Objects.requireNonNull(clusterOneReactiveSchedulerLockExecutor);
    }

    @POST
    @Path("interceptor/default")
    @MongoSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMongoClient() {
        doSomething();
    }

    @POST
    @Path("interceptor/clusterOne")
    @MongoSchedulerLock(mongoClientName = "cluster1", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMongoClientClusterOne() {
        doSomething();
    }

    @POST
    @Path("interceptor/reactive/default")
    @MongoReactiveSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingReactiveMongoClient() {
        doSomething();
    }

    @POST
    @Path("interceptor/reactive/clusterOne")
    @MongoReactiveSchedulerLock(mongoClientName = "cluster1", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingReactiveMongoClientClusterOne() {
        doSomething();
    }

    @POST
    @Path("execute/default")
    public ExecutionResultDTO executeSomethingUsingMongoClient() {
        final TaskResult<Integer> result = defaultSchedulerLockExecutor.executeWithLock(this::doSomething,
                "counterDefaultMongo");
        return new ExecutionResultDTO(result);
    }

    @POST
    @Path("execute/clusterOne")
    public ExecutionResultDTO executeSomethingUsingMongoClientClusterOne() {
        final TaskResult<Integer> result = clusterOneSchedulerLockExecutor.executeWithLock(this::doSomething,
                "counterClusterOneMongo");
        return new ExecutionResultDTO(result);
    }

    @POST
    @Path("execute/reactive/default")
    public ExecutionResultDTO executeSomethingUsingReactiveMongoClient() {
        final TaskResult<Integer> result = defaultReactiveSchedulerLockExecutor.executeWithLock(this::doSomething,
                "counterDefaultReactiveMongo");
        return new ExecutionResultDTO(result);
    }

    @POST
    @Path("execute/reactive/clusterOne")
    public ExecutionResultDTO executeSomethingUsingReactiveMongoClientClusterOne() {
        final TaskResult<Integer> result = clusterOneReactiveSchedulerLockExecutor.executeWithLock(this::doSomething,
                "counterClusterOneReactiveMongo");
        return new ExecutionResultDTO(result);
    }

    @Override
    void resetLockStorage() {
        defaultMongoClient.getDatabase("shedLock").drop();
        clusterOneMongoClient.getDatabase("customDatabase").drop();
    }
}
