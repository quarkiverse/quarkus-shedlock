package io.quarkiverse.shedlock.providers.mongo.reactive.deployment.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.test.QuarkusUnitTest;

class CustomInstanceAndDatabaseTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    // language=properties
                    .addAsResource(new StringAsset("""
                            quarkus.shedlock.defaults-lock-at-most-for=PT30S
                            quarkus.shedlock.mongo-reactive.cluster1.database-name=customDatabase"""),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));
    @Inject
    CustomLockableResource customLockableResource;

    @Inject
    @MongoClientName("cluster1") // must be declared to start a specific mongo dev service for cluster1reactive
    ReactiveMongoClient clusterOne;

    @Test
    void shouldUseCustomInstanceAndDatabase() {
        customLockableResource.doSomething();

        final List<String> databaseNames = clusterOne.listDatabaseNames().collect().asList().await().indefinitely();
        assertThat(databaseNames).contains("customDatabase");
    }

    @BeforeEach
    @AfterEach
    void drop() {
        clusterOne.getDatabase("customDatabase").drop().await().indefinitely();
    }

    @ApplicationScoped
    static class CustomLockableResource {

        @MongoReactiveSchedulerLock(mongoClientName = "cluster1")
        void doSomething() {
        }
    }
}
