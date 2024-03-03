package io.quarkiverse.shedlock.providers.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.agroal.api.AgroalDataSource;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

public class JdbcSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Inject
    AgroalDataSource agroalDataSource;

    @Inject
    LockableService lockableService;

    @Test
    public void shouldUseDefaultTableName() {
        final List<String> tablesName = new ArrayList<>();
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement selectTablesNameStatement = connection.prepareStatement(
                        "SELECT table_name FROM information_schema.tables")) {
            final ResultSet tablesNameResultSet = selectTablesNameStatement.executeQuery();
            while (tablesNameResultSet.next()) {
                tablesName.add(tablesNameResultSet.getString("table_name"));
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(tablesName).contains("shedlock");
    }

    @Test
    public void shouldCreateALock() {
        lockableService.execute();

        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'io.quarkiverse.shedlock.providers.jdbc.deployment.LockableService_execute'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @AfterEach
    public void tearDown() {
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
