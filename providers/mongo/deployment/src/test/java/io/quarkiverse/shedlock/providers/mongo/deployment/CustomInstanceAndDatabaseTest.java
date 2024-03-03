package io.quarkiverse.shedlock.providers.mongo.deployment;

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
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.test.QuarkusUnitTest;

public class CustomInstanceAndDatabaseTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(CustomLockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S\n" +
                            "quarkus.shedlock.mongo.mongoclients.cluster1.database-name=customDatabase"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));
    @Inject
    CustomLockableService lockableService;

    @Inject
    @MongoClientName("cluster1") // must be declared to start a specific mongo dev service for cluster1
    MongoClient clusterOne;

    @Test
    public void shouldUseCustomInstanceAndDatabase() {
        lockableService.execute();

        assertThat(clusterOne.listDatabaseNames()).contains("customDatabase");
    }

    @AfterEach
    public void tearDown() {
        clusterOne.getDatabase("customDatabase").drop();
    }
}
