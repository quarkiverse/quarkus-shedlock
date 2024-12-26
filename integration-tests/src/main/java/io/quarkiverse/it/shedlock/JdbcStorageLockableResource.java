package io.quarkiverse.it.shedlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLockExecutor;
import io.quarkus.agroal.DataSource;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;

@ApplicationScoped
@Path("/jdbcStorageLockableResource")
public class JdbcStorageLockableResource extends AbstractLockableResource {
    private final AgroalDataSource defaultDataSource;
    private final AgroalDataSource masterDataSource;
    private final SchedulerLockExecutor defaultSchedulerLockExecutor;
    private final SchedulerLockExecutor masterSchedulerLockExecutor;

    public JdbcStorageLockableResource(final AgroalDataSource defaultDataSource,
            @DataSource("master") final AgroalDataSource masterDataSource,
            @JdbcSchedulerLockExecutor(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor defaultSchedulerLockExecutor,
            @JdbcSchedulerLockExecutor(dataSourceName = "master", lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor masterSchedulerLockExecutor) {
        this.defaultDataSource = Objects.requireNonNull(defaultDataSource);
        this.masterDataSource = Objects.requireNonNull(masterDataSource);
        this.defaultSchedulerLockExecutor = Objects.requireNonNull(defaultSchedulerLockExecutor);
        this.masterSchedulerLockExecutor = Objects.requireNonNull(masterSchedulerLockExecutor);
    }

    @POST
    @Path("interceptor/default")
    @JdbcSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingDefaultJdbcStorageForLock() {
        doSomething();
    }

    @POST
    @Path("interceptor/master")
    @JdbcSchedulerLock(dataSourceName = "master", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMasterJdbcStorageForLock() {
        doSomething();
    }

    @POST
    @Path("execute/default")
    public ExecutionResultDTO executeSomethingUsingDefaultJdbcStorageForLock() {
        final TaskResult<Integer> result = defaultSchedulerLockExecutor.executeWithLock(this::doSomething,
                "counterDefaultJdbc");
        return new ExecutionResultDTO(result);
    }

    @POST
    @Path("execute/master")
    public ExecutionResultDTO executeSomethingUsingMasterJdbcStorageForLock() {
        final TaskResult<Integer> result = masterSchedulerLockExecutor.executeWithLock(this::doSomething, "counterMasterJdbc");
        return new ExecutionResultDTO(result);
    }

    @Override
    void resetLockStorage() {
        try (final Connection connection = defaultDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        try (final Connection connection = masterDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE myShedLockTableName")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
