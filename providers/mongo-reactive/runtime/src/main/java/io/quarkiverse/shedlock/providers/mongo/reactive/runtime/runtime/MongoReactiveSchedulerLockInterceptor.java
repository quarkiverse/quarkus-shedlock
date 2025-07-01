package io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Default;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.arc.Arc;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoDatabase;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.reactivestreams.ReactiveStreamsMongoLockProvider;

@MongoReactiveSchedulerLock
@Priority(3001)
@Interceptor
public class MongoReactiveSchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    private final MongoReactiveConfig mongoReactiveConfig;

    protected MongoReactiveSchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final MongoReactiveConfig mongoReactiveConfig) {
        super(shedLockConfiguration, instantProvider);
        this.mongoReactiveConfig = Objects.requireNonNull(mongoReactiveConfig);
    }

    @Override
    @AroundInvoke
    protected Object lock(InvocationContext context) throws Throwable {
        return super.lock(context);
    }

    @Override
    protected LockProvider lockProvider(final Method method) {
        final String mongoClientName = method.getAnnotation(MongoReactiveSchedulerLock.class).mongoClientName();
        final ReactiveMongoClient mongoClient = Arc.container()
                .select(ReactiveMongoClient.class,
                        MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME.equals(mongoClientName) ? new Default.Literal()
                                : new MongoClientName.Literal(mongoClientName))
                .get();
        final String databaseName = Optional.ofNullable(mongoReactiveConfig.mongoclients().get(mongoClientName))
                .map(MongoReactiveConfig.MongoClientConfig::databaseName)
                .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
        final ReactiveMongoDatabase database = mongoClient.getDatabase(databaseName);
        return new ReactiveStreamsMongoLockProvider(database.unwrap());
    }

    @Override
    protected LockDuration lockDuration(final Method method) {
        return method.getAnnotation(MongoReactiveSchedulerLock.class).lockDuration();
    }
}
