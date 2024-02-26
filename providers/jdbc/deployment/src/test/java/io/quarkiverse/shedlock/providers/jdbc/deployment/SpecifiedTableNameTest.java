package io.quarkiverse.shedlock.providers.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

public class SpecifiedTableNameTest extends TestBase {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset("quarkus.shedlock.jdbc.table-name=myShedLockTableName"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Test
    public void shouldUseSpecifiedTableName() {
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

        assertThat(tablesName).contains("myshedlocktablename");
    }
}
