package io.quarkiverse.shedlock.providers.inmemory.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockExecutor;
import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskWithResult;

class InMemorySchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S"),
                            "application.properties"));

    @Inject
    LockableResource lockableResource;

    @Inject
    @InMemorySchedulerLockExecutor
    SchedulerLockExecutor schedulerLockExecutor;

    @Test
    void shouldLockUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            lockableResource.doSomething();
        }

        assertThat(lockableResource.getCallCount()).isEqualTo(1);
    }

    @Test
    void shouldLockUsingExecutor() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final TaskWithResult<Integer> counterTask = counter::incrementAndGet;

        final List<TaskResult<Integer>> results = new ArrayList<>();

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

    @ApplicationScoped
    static class LockableResource {
        private final AtomicInteger callCounter = new AtomicInteger();

        @InMemorySchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
        void doSomething() {
            callCounter.incrementAndGet();
        }

        public Integer getCallCount() {
            return callCounter.get();
        }
    }
}
