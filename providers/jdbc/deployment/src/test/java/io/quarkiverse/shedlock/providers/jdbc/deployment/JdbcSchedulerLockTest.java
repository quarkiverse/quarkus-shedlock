package io.quarkiverse.shedlock.providers.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLockExecutor;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskWithResult;

class JdbcSchedulerLockTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableResource.class)
                    // language=properties
                    .addAsResource(new StringAsset("""
                            quarkus.shedlock.defaults-lock-at-most-for=PT30S
                            quarkus.datasource.devservices.reuse=false"""),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Inject
    AgroalDataSource agroalDataSource;

    @Inject
    LockableResource lockableResource;

    @Inject
    @JdbcSchedulerLockExecutor(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S")
    SchedulerLockExecutor schedulerLockExecutor;

    @Test
    void shouldLockUsingInterceptor() {
        for (int called = 0; called < 5; called++) {
            lockableResource.doSomething();
        }

        assertThat(lockableResource.getCallCount()).isEqualTo(1);
    }

    @Test
    void shouldLockUsingInterceptorCreateALock() {
        lockableResource.doSomething();

        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'io.quarkiverse.shedlock.providers.jdbc.deployment.LockableResource_doSomething'");
                final ResultSet countLocksResultSet = countLocksStatement.executeQuery()) {
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldLockUsingExecutor() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final TaskWithResult<Integer> counterTask = counter::incrementAndGet;

        final List<TaskResult<Integer>> results = new ArrayList<>();

        // When
        for (int execution = 0; execution < 5; execution++) {
            results.add(schedulerLockExecutor.executeWithLock(counterTask, "counter"));
        }

        // Then
        assertAll(
                () -> assertThat(results.size()).isEqualTo(5),
                () -> assertThat(results.get(0).wasExecuted()).isTrue(),
                () -> assertThat(results.get(0).getResult()).isEqualTo(1),
                () -> assertThat(results.get(1).wasExecuted()).isFalse(),
                () -> assertThat(results.get(1).getResult()).isNull(),
                () -> assertThat(results.get(2).wasExecuted()).isFalse(),
                () -> assertThat(results.get(2).getResult()).isNull(),
                () -> assertThat(results.get(3).wasExecuted()).isFalse(),
                () -> assertThat(results.get(3).getResult()).isNull(),
                () -> assertThat(results.get(4).wasExecuted()).isFalse(),
                () -> assertThat(results.get(4).getResult()).isNull());
    }

    @Test
    void shouldLockUsingExecutorCreateALock() {
        // Given
        final AtomicInteger counter = new AtomicInteger(0);
        final TaskWithResult<Integer> counterTask = counter::incrementAndGet;

        // When
        // need to use another lock name because the JdbcLockProvider extends StorageBasedLockProvider
        // which hold lock references locally
        final TaskResult<Integer> integerTaskResult = schedulerLockExecutor.executeWithLock(counterTask, "counter2");
        Validate.validState(integerTaskResult.wasExecuted());

        // Then
        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'counter2'");
                final ResultSet countLocksResultSet = countLocksStatement.executeQuery()) {
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldUseDefaultTableName() {
        final List<String> tablesName = new ArrayList<>();
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement selectTablesNameStatement = connection.prepareStatement(
                        "SELECT table_name FROM information_schema.tables");
                final ResultSet tablesNameResultSet = selectTablesNameStatement.executeQuery()) {
            while (tablesNameResultSet.next()) {
                tablesName.add(tablesNameResultSet.getString("table_name"));
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(tablesName).contains("shedlock");
    }

    @AfterEach
    void tearDown() {
        lockableResource.reset();
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
