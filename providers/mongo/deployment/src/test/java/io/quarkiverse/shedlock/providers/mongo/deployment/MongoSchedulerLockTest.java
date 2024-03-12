package io.quarkiverse.shedlock.providers.mongo.deployment;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.mongodb.client.MongoClient;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

public class MongoSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));

    @Inject
    LockableService lockableService;

    @Inject
    MongoClient mongoClient;

    @Test
    public void shouldUseDefaultDatabaseName() {
        lockableService.execute();

        assertThat(mongoClient.listDatabaseNames()).contains("shedLock");
    }

    @Test
    public void shouldCreateALock() {
        lockableService.execute();

        assertThat(mongoClient.getDatabase("shedLock").getCollection("shedLock")
                .countDocuments(eq("_id", "io.quarkiverse.shedlock.providers.mongo.deployment.LockableService_execute")))
                .isEqualTo(1L);
    }

    @AfterEach
    public void tearDown() {
        mongoClient.getDatabase("shedLock").drop();
    }
}
