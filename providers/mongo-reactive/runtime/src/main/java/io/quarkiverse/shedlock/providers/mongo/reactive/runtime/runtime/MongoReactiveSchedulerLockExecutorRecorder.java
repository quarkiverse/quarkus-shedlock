package io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime;

import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.inject.Default;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.arc.Arc;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoDatabase;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;
import io.quarkus.runtime.annotations.Recorder;
import net.javacrumbs.shedlock.provider.mongo.reactivestreams.ReactiveStreamsMongoLockProvider;

@Recorder
public class MongoReactiveSchedulerLockExecutorRecorder {

    public Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor> schedulerLockExecutorSupplier(
            final ShedLockConfiguration shedLockConfiguration,
            final MongoReactiveConfig mongoReactiveConfig,
            final String mongoClientName,
            final String lockAtMostFor,
            final String lockAtLeastFor) {
        return new Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor>() {
            @Override
            public SchedulerLockExecutor apply(final SyntheticCreationalContext<SchedulerLockExecutor> context) {
                final ReactiveMongoClient mongoClient = Arc.container()
                        .select(ReactiveMongoClient.class,
                                MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME.equals(mongoClientName) ? new Default.Literal()
                                        : new MongoClientName.Literal(mongoClientName))
                        .get();
                final String databaseName = Optional.ofNullable(mongoReactiveConfig.mongoclients().get(mongoClientName))
                        .map(MongoReactiveConfig.MongoClientConfig::databaseName)
                        .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
                final ReactiveMongoDatabase database = mongoClient.getDatabase(databaseName);
                return new SchedulerLockExecutor(
                        shedLockConfiguration,
                        context.getInjectedReference(InstantProvider.class),
                        new ReactiveStreamsMongoLockProvider(database.unwrap()),
                        lockAtMostFor, lockAtLeastFor);
            }
        };
    }
}
