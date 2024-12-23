package io.quarkiverse.shedlock.common.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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
    void shouldIntercept() {
        // Given

        // When
        lockableService.execute();

        // Then
        assertThat(stubbedLockProvider.hasBeenCalled()).isTrue();
    }
}
