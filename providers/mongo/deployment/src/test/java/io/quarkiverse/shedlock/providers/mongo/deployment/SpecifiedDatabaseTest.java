package io.quarkiverse.shedlock.providers.mongo.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;

public class SpecifiedDatabaseTest extends TestBase {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.mongodb.database=myShedLockDatabase"),
                            "application.properties"));

    @Inject
    Instance<LockProvider> lockProvider;

    @Test
    public void shouldUseSpecifiedDatabaseName() {
        // need to provoke a store for database creation ... mongodb behavior
        lockProvider.get().lock(new LockConfiguration(Instant.now(), "shouldUseSpecifiedDatabaseName", Duration.ofSeconds(10),
                Duration.ofSeconds(10)));

        assertThat(mongoClient.listDatabaseNames()).contains("myShedLockDatabase");
    }
}
