package io.quarkiverse.shedlock.providers.inmemory.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;
import io.quarkus.test.QuarkusUnitTest;

class InMemorySchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S"),
                            "application.properties"));

    @Inject
    LockableResource lockableResource;

    @Test
    void shouldLock() {
        for (int called = 0; called < 5; called++) {
            lockableResource.doSomething();
        }

        assertThat(lockableResource.getCallCount()).isEqualTo(1);
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
