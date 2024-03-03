package io.quarkiverse.shedlock.common.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class ShouldFailWhenDefaultLockAtMostForPropertyIsMissingTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(
                            DefaultSchedulerLock.class,
                            DefaultLockProvider.class,
                            DefaultSchedulerLockInterceptor.class,
                            LockableService.class))
            .assertException(throwable -> assertThat(throwable)
                    .hasNoSuppressedExceptions()
                    .rootCause()
                    .hasMessage("Configuration validation failed:\n" +
                            "\tjava.util.NoSuchElementException: SRCFG00014: The config property quarkus.shedlock.defaults-lock-at-most-for is required but it could not be found in any config source")
                    .hasNoSuppressedExceptions());

    @Test
    public void test() {
        Assertions.fail("Startup should have failed");
    }
}
