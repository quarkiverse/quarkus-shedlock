package io.quarkiverse.shedlock.providers.inmemory.deployment;

import io.quarkus.arc.ClientProxy;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InMemoryShedlockTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    Instance<LockProvider> lockProvider;

    @Test
    public void shouldProduceExpectedLockProvider() {
        assertAll(
                () -> assertThat(lockProvider.isResolvable()).isTrue(),
                () -> assertThat(((ClientProxy) lockProvider.get()).arc_contextualInstance()).isInstanceOf(InMemoryLockProvider.class));
    }
}
