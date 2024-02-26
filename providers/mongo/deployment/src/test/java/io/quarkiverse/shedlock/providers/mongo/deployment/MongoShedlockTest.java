package io.quarkiverse.shedlock.providers.mongo.deployment;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.time.Instant;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.ClientProxy;
import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

public class MongoShedlockTest extends TestBase {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    Instance<LockProvider> lockProvider;

    @Test
    public void shouldProduceExpectedLockProvider() {
        assertAll(
                () -> assertThat(lockProvider.isResolvable()).isTrue(),
                () -> assertThat(((ClientProxy) lockProvider.get()).arc_contextualInstance())
                        .isInstanceOf(MongoLockProvider.class));
    }

    @Test
    public void shouldUseDefaultDatabaseName() {
        // need to provoke a store for database creation ... mongodb behavior
        lockProvider.get().lock(new LockConfiguration(Instant.now(), "shouldUseDefaultDatabaseName", Duration.ofSeconds(10),
                Duration.ofSeconds(5)));

        assertAll(
                () -> assertThat(mongoClient.listDatabaseNames()).contains("shedLock"),
                () -> assertThat(mongoDatabase.getName()).isEqualTo("shedLock"));
    }

    @Test
    public void shouldCreateALock() {
        lockProvider.get().lock(new LockConfiguration(Instant.ofEpochMilli(1000), "shouldCreateALock", Duration.ofSeconds(10),
                Duration.ofSeconds(5)));

        assertThat(mongoDatabase.getCollection("shedLock").countDocuments(eq("_id", "shouldCreateALock")))
                .isEqualTo(1L);
    }
}
