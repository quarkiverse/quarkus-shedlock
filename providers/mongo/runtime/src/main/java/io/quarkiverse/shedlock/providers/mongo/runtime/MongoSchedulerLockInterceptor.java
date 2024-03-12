package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Default;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.arc.Arc;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

@MongoSchedulerLock
@Priority(3001)
@Interceptor
public class MongoSchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    private final MongoConfig mongoConfig;

    protected MongoSchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final MongoConfig mongoConfig) {
        super(shedLockConfiguration, instantProvider);
        this.mongoConfig = Objects.requireNonNull(mongoConfig);
    }

    @Override
    @AroundInvoke
    protected Object lock(InvocationContext context) throws Throwable {
        return super.lock(context);
    }

    @Override
    protected LockProvider lockProvider(final Method method) {
        final String mongoClientName = method.getAnnotation(MongoSchedulerLock.class).mongoClientName();
        final MongoClient mongoClient = Arc.container()
                .select(MongoClient.class,
                        MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME.equals(mongoClientName) ? new Default.Literal()
                                : new MongoClientName.Literal(mongoClientName))
                .get();
        final String databaseName = Optional.ofNullable(mongoConfig.mongoclients().get(mongoClientName))
                .map(MongoConfig.MongoClientConfig::databaseName)
                .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
        final MongoDatabase database = mongoClient.getDatabase(databaseName);
        return new MongoLockProvider(database);
    }

    @Override
    protected LockDuration lockDuration(final Method method) {
        return method.getAnnotation(MongoSchedulerLock.class).lockDuration();
    }
}
