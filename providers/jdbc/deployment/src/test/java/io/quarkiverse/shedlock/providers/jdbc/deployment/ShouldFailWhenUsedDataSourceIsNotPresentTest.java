package io.quarkiverse.shedlock.providers.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

public class ShouldFailWhenUsedDataSourceIsNotPresentTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S\n" +
                            "quarkus.shedlock.jdbc.unknownDataSource.table-name=myShedLockTableName"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())))
            .assertException(throwable -> assertThat(throwable)
                    .hasNoSuppressedExceptions()
                    .rootCause()
                    .hasMessage(
                            "A missing datasource 'unknownDataSource' has been defined for ShedLock. Please fixe the ShedLock configuration or add the datasource")
                    .hasNoSuppressedExceptions());

    @Test
    public void test() {
        Assertions.fail("Startup should have failed");
    }
}
