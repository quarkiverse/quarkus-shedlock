package io.quarkiverse.shedlock.providers.jdbc.deployment;

import io.agroal.api.AgroalDataSource;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ShouldNotCreateTheTableWhenNotWantedTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(LockableService.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.defaults-lock-at-most-for=PT30S\n" +
                                                   "quarkus.shedlock.jdbc.enable-table-creation=false\n" +
                                                   "quarkus.shedlock.jdbc.table-name=shouldNotExists"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Inject
    AgroalDataSource defaultAgroalDataSource;

    @Test
    public void shouldUseSpecifiedTableName() {
        final List<String> tablesName = new ArrayList<>();
        try (final Connection connection = defaultAgroalDataSource.getConnection();
             final PreparedStatement selectTablesNameStatement = connection.prepareStatement(
                     "SELECT table_name FROM information_schema.tables")) {
            final ResultSet tablesNameResultSet = selectTablesNameStatement.executeQuery();
            while (tablesNameResultSet.next()) {
                tablesName.add(tablesNameResultSet.getString("table_name"));
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(tablesName).doesNotContain("shouldnotexists");
    }
}
