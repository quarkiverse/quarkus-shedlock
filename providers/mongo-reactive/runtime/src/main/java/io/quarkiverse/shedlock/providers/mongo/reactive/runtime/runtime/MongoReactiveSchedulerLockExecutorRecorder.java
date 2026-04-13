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
import io.quarkus.runtime.annotations.Recorder;
import net.javacrumbs.shedlock.provider.mongo.reactivestreams.ReactiveStreamsMongoLockProvider;

@Recorder
public class MongoReactiveSchedulerLockExecutorRecorder {

    public Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor> schedulerLockExecutorSupplier(
            final String mongoClientName) {
        return new Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor>() {
            @Override
            public SchedulerLockExecutor apply(final SyntheticCreationalContext<SchedulerLockExecutor> context) {

                MongoReactiveConfig mongoReactiveConfig = context.getInjectedReference(MongoReactiveConfig.class);

                final ReactiveMongoClient mongoClient = Arc.container()
                        .select(ReactiveMongoClient.class,
                                io.quarkus.mongodb.runtime.MongoConfig.DEFAULT_REACTIVE_CLIENT_NAME.equals(mongoClientName)
                                        ? new Default.Literal()
                                        : new MongoClientName.Literal(mongoClientName))
                        .get();
                final String databaseName = Optional.ofNullable(mongoReactiveConfig.mongoclients().get(mongoClientName))
                        .map(MongoReactiveConfig.MongoClientConfig::databaseName)
                        .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
                final ReactiveMongoDatabase database = mongoClient.getDatabase(databaseName);
                return new SchedulerLockExecutor(
                        context.getInjectedReference(ShedLockConfiguration.class),
                        context.getInjectedReference(InstantProvider.class),
                        new ReactiveStreamsMongoLockProvider(database.unwrap()));
            }
        };
    }
}
