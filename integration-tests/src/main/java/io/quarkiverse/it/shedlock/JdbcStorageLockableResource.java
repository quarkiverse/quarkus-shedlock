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
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;
import io.quarkus.agroal.DataSource;

@ApplicationScoped
@Path("/jdbcStorageLockableResource")
public class JdbcStorageLockableResource extends AbstractLockableResource {
    private final AgroalDataSource defaultDataSource;
    private final AgroalDataSource masterDataSource;

    public JdbcStorageLockableResource(final AgroalDataSource defaultDataSource,
            @DataSource("master") final AgroalDataSource masterDataSource) {
        this.defaultDataSource = Objects.requireNonNull(defaultDataSource);
        this.masterDataSource = Objects.requireNonNull(masterDataSource);
    }

    @POST
    @Path("default")
    @JdbcSchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingDefaultJdbcStorageForLock() {
        doSomething();
    }

    @POST
    @Path("master")
    @JdbcSchedulerLock(dataSourceName = "master", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingMasterJdbcStorageForLock() {
        doSomething();
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
