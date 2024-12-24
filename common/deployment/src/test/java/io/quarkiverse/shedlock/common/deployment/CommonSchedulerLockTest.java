package io.quarkiverse.shedlock.common.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.common.runtime.LockingNotSupportedException;
import io.quarkus.test.QuarkusUnitTest;

class CommonSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(
                            TestSchedulerLock.class,
                            StubbedLockProvider.class,
                            TestSchedulerLockInterceptor.class,
                            LockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S\n" +
                            "quarkus.datasource.devservices.reuse=false"),
                            "application.properties"));

    @Inject
    LockableService lockableService;

    @Inject
    StubbedLockProvider stubbedLockProvider;

    @Test
    void shouldLockThenUnlock() {
        // Given

        // When
        lockableService.execute();

        // Then
        final String lockName = "io.quarkiverse.shedlock.common.deployment.LockableService_execute";
        assertAll(
                () -> assertThat(stubbedLockProvider.hasBeenLocked(lockName)).isTrue(),
                () -> assertThat(stubbedLockProvider.hasBeenUnlocked(lockName)).isTrue());
    }

    @Test
    void shouldFailWhenReturnTypeIsNotVoid() {
        // Given

        // When && Then
        assertAll(
                () -> assertThatThrownBy(() -> lockableService.unsupportedReturn())
                        .isInstanceOf(LockingNotSupportedException.class)
                        .hasMessage("Can not lock method returning value (do not know what to return if it's locked)"),
                () -> assertThat(stubbedLockProvider.hasBeenLocked()).isFalse());
    }

    @Test
    void shouldUnLockWhenAnExceptionOccurred() {
        // Given

        // When && Then
        final String lockName = "io.quarkiverse.shedlock.common.deployment.LockableService_exception";
        assertAll(
                () -> assertThatThrownBy(() -> lockableService.exception())
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
