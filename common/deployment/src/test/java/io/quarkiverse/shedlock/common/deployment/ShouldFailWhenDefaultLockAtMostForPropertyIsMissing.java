package io.quarkiverse.shedlock.common.deployment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class ShouldFailWhenDefaultLockAtMostForPropertyIsMissing {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(DefaultLockProvider.class)
                    .addClass(LockableService.class));

    @Inject
    LockableService lockableService;

    @Test
    public void shouldFail() {
        // Given

        // When & Then
        assertThatThrownBy(() -> lockableService.execute())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("shedlock.defaults.lock-at-most-for parameter is mandatory");
    }

}
