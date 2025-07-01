package io.quarkiverse.shedlock.providers.mongo.reactive.deployment.deployment;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLockExecutor;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

class MongoReactiveSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableResource.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));

    @Inject
    LockableResource lockableResource;

    @Inject
    ReactiveMongoClient reactiveMongoClient;

    @Inject
    @MongoReactiveSchedulerLockExecutor
    SchedulerLockExecutor schedulerLockExecutor;

    @Test
    void shouldLockUsingInterceptor() {
        for (int called = 0; called < 5; called++) {
            lockableResource.doSomething();
        }
        assertThat(lockableResource.getCallCount()).isEqualTo(1);
    }

    @Test
    void shouldLockUsingInterceptorCreateALock() {
        lockableResource.doSomething();

        final Long documents = reactiveMongoClient.getDatabase("shedLock").getCollection("shedLock")
                .countDocuments(eq("_id",
                        "io.quarkiverse.shedlock.providers.mongo.reactive.deployment.deployment.LockableResource_doSomething"))
                .await().indefinitely();
        assertThat(documents).isEqualTo(1L);
    }

    @Test
    void shouldLockUsingExecutor() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final LockingTaskExecutor.TaskWithResult<Integer> counterTask = counter::incrementAndGet;

        final List<LockingTaskExecutor.TaskResult<Integer>> results = new ArrayList<>();

        // When
        for (int execution = 0; execution < 5; execution++) {
            results.add(schedulerLockExecutor.executeWithLock(counterTask, "counter",
                    Duration.ofSeconds(30), Duration.ofSeconds(10)));
        }

        // Then
        assertAll(
                () -> assertThat(results.size()).isEqualTo(5),
                () -> assertThat(results.get(0).wasExecuted()).isTrue(),
                () -> assertThat(results.get(0).getResult()).isEqualTo(1),
                () -> assertThat(results.get(1).wasExecuted()).isFalse(),
                () -> assertThat(results.get(1).getResult()).isNull(),
                () -> assertThat(results.get(2).wasExecuted()).isFalse(),
                () -> assertThat(results.get(2).getResult()).isNull(),
                () -> assertThat(results.get(3).wasExecuted()).isFalse(),
                () -> assertThat(results.get(3).getResult()).isNull(),
                () -> assertThat(results.get(4).wasExecuted()).isFalse(),
                () -> assertThat(results.get(4).getResult()).isNull());
    }

    @Test
    void shouldLockUsingExecutorCreateALock() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final LockingTaskExecutor.TaskWithResult<Integer> counterTask = counter::incrementAndGet;

        // When
        final LockingTaskExecutor.TaskResult<Integer> integerTaskResult = schedulerLockExecutor.executeWithLock(counterTask,
                "counter", Duration.ofSeconds(30), Duration.ofSeconds(10));
        Validate.validState(integerTaskResult.wasExecuted());

        // Then
        assertThat(reactiveMongoClient.getDatabase("shedLock").getCollection("shedLock")
                .countDocuments(eq("_id", "counter")).await().indefinitely())
                .isEqualTo(1L);
    }

    @Test
    void shouldUseDefaultDatabaseName() {
        lockableResource.doSomething();

        final List<String> databaseNames = reactiveMongoClient.listDatabaseNames().collect().asList().await().indefinitely();
        assertThat(databaseNames).contains("shedLock");
    }

    @BeforeEach
    @AfterEach
    void dropCollections() {
        lockableResource.reset();
        reactiveMongoClient.getDatabase("shedLock").drop().await().indefinitely();
    }
}
