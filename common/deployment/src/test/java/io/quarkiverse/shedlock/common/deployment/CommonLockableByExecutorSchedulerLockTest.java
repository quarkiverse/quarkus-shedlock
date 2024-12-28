package io.quarkiverse.shedlock.common.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskWithResult;

class CommonLockableByExecutorSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(
                            TestSchedulerLock.class,
                            StubbedLockProvider.class,
                            SchedulerLockExecutorProducer.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S\n" +
                            "quarkus.datasource.devservices.reuse=false"),
                            "application.properties"));

    @Inject
    StubbedLockProvider stubbedLockProvider;

    @Inject
    SchedulerLockExecutor schedulerLockExecutor;

    @Test
    void shouldLockThenUnlock() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final TaskWithResult<Integer> counterTask = counter::incrementAndGet;
        final String lockName = "counter";

        // When
        final TaskResult<Integer> result = schedulerLockExecutor.executeWithLock(counterTask, lockName, null, null);

        // Then
        assertAll(
                () -> assertThat(result.wasExecuted()).isTrue(),
                () -> assertThat(result.getResult()).isEqualTo(1),
                () -> assertThat(stubbedLockProvider.hasBeenLocked(lockName)).isTrue(),
                () -> assertThat(stubbedLockProvider.hasBeenUnlocked(lockName)).isTrue());
    }

    @Test
    void shouldUnLockWhenAnExceptionOccurred() {
        // Given
        final TaskWithResult<Integer> counterTask = () -> {
            throw new RuntimeException("Something went wrong");
        };
        final String lockName = "counter";

        // When && Then
        assertAll(
                () -> assertThatThrownBy(() -> schedulerLockExecutor.executeWithLock(counterTask, lockName, null, null))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Something went wrong"),
                () -> assertThat(stubbedLockProvider.hasBeenLocked(lockName)).isTrue(),
                () -> assertThat(stubbedLockProvider.hasBeenUnlocked(lockName)).isTrue());
    }

    @BeforeEach
    @AfterEach
    void cleanup() {
        stubbedLockProvider.reset();
    }
}
