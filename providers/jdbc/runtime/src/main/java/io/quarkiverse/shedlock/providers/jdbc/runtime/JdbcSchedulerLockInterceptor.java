package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Default;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;

@JdbcSchedulerLock
@Priority(3001)
@Interceptor
public class JdbcSchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    private final JdbcConfig jdbcConfig;

    public JdbcSchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final JdbcConfig jdbcConfig) {
        super(shedLockConfiguration, instantProvider);
        this.jdbcConfig = Objects.requireNonNull(jdbcConfig);
    }

    @Override
    @AroundInvoke
    protected Object lock(InvocationContext context) throws Throwable {
        return super.lock(context);
    }

    @Override
    protected LockProvider lockProvider(final Method method) {
        final String dataSourceName = method.getAnnotation(JdbcSchedulerLock.class).dataSourceName();
        final AgroalDataSource agroalDataSource = Arc.container()
                .select(AgroalDataSource.class, JdbcConfig.DEFAULT.equals(dataSourceName) ? new Default.Literal()
                        : new DataSource.DataSourceLiteral(dataSourceName))
                .get();
        final String tableName = Optional.ofNullable(jdbcConfig.datasources().get(dataSourceName))
                .map(JdbcConfig.DataSourceConfig::tableName)
                .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
        return new JdbcLockProvider(agroalDataSource, tableName);
    }

    @Override
    protected LockDuration lockDuration(final Method method) {
        return method.getAnnotation(JdbcSchedulerLock.class).lockDuration();
    }
}
