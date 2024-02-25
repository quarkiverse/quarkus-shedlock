package io.quarkiverse.shedlock.providers.jdbc.deployment;

import io.quarkus.arc.ClientProxy;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class JdbcShedlockTest extends TestBase {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Inject
    Instance<LockProvider> lockProvider;

    @Test
    public void shouldProduceExpectedLockProvider() {
        assertAll(
                () -> assertThat(lockProvider.isResolvable()).isTrue(),
                () -> assertThat(((ClientProxy) lockProvider.get()).arc_contextualInstance()).isInstanceOf(JdbcLockProvider.class));
    }

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
        lockProvider.get().lock(new LockConfiguration(Instant.ofEpochMilli(1000), "shouldCreateALock", Duration.ofSeconds(10),
                Duration.ofSeconds(5)));

        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
             final PreparedStatement countLocksStatement = connection.prepareStatement(
                     "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'shouldCreateALock'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }
}
