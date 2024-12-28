package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.inject.Default;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.runtime.annotations.Recorder;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;

@Recorder
public class JdbcSchedulerLockExecutorRecorder {

    public Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor> schedulerLockExecutorSupplier(
            final ShedLockConfiguration shedLockConfiguration,
            final JdbcConfig jdbcConfig,
            final String dataSourceName) {
        return new Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor>() {
            @Override
            public SchedulerLockExecutor apply(final SyntheticCreationalContext<SchedulerLockExecutor> context) {
                final String tableName = Optional.ofNullable(jdbcConfig.dataSources().get(dataSourceName))
                        .map(JdbcConfig.DataSourceConfig::tableName)
                        .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
                final AgroalDataSource agroalDataSource = Arc.container()
                        .select(AgroalDataSource.class,
                                DataSourceUtil.DEFAULT_DATASOURCE_NAME.equals(dataSourceName) ? new Default.Literal()
                                        : new DataSource.DataSourceLiteral(dataSourceName))
                        .get();
                return new SchedulerLockExecutor(
                        shedLockConfiguration,
                        context.getInjectedReference(InstantProvider.class),
                        new JdbcLockProvider(agroalDataSource, tableName));
            }
        };
    }
}
