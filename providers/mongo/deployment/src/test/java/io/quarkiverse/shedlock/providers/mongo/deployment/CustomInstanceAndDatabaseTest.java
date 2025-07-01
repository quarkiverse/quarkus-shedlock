package io.quarkiverse.shedlock.providers.mongo.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.StreamSupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.mongodb.client.MongoClient;

import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.test.QuarkusUnitTest;

class CustomInstanceAndDatabaseTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(CustomLockableResource.class)
                    // language=properties
                    .addAsResource(new StringAsset("""
                            quarkus.shedlock.defaults-lock-at-most-for=PT30S
                            quarkus.shedlock.mongo.cluster1.database-name=customDatabase"""),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));
    @Inject
    CustomLockableResource lockableService;

    @Inject
    @MongoClientName("cluster1") // must be declared to start a specific mongo dev service for cluster1
    MongoClient clusterOne;

    @Test
    void shouldUseCustomInstanceAndDatabase() {
        lockableService.doSomething();

        final List<String> databaseNames = StreamSupport.stream(
                clusterOne.listDatabaseNames().spliterator(), false)
                .toList();
        assertThat(databaseNames).contains("customDatabase");
    }

    @AfterEach
    void tearDown() {
        clusterOne.getDatabase("customDatabase").drop();
    }

    @ApplicationScoped
    static class CustomLockableResource {

        @MongoSchedulerLock(mongoClientName = "cluster1")
        void doSomething() {
        }
    }
}
