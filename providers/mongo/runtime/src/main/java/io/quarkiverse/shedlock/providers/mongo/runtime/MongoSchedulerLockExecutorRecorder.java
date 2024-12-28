package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.util.Optional;
import java.util.function.Function;

import jakarta.enterprise.inject.Default;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.arc.Arc;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.mongodb.MongoClientName;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;
import io.quarkus.runtime.annotations.Recorder;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

@Recorder
public class MongoSchedulerLockExecutorRecorder {

    public Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor> schedulerLockExecutorSupplier(
            final ShedLockConfiguration shedLockConfiguration,
            final MongoConfig mongoConfig,
            final String mongoClientName) {
        return new Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor>() {
            @Override
            public SchedulerLockExecutor apply(final SyntheticCreationalContext<SchedulerLockExecutor> context) {
                final MongoClient mongoClient = Arc.container()
                        .select(MongoClient.class,
                                MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME.equals(mongoClientName) ? new Default.Literal()
                                        : new MongoClientName.Literal(mongoClientName))
                        .get();
                final String databaseName = Optional.ofNullable(mongoConfig.mongoclients().get(mongoClientName))
                        .map(MongoConfig.MongoClientConfig::databaseName)
                        .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
                final MongoDatabase database = mongoClient.getDatabase(databaseName);
                return new SchedulerLockExecutor(
                        shedLockConfiguration,
                        context.getInjectedReference(InstantProvider.class),
                        new MongoLockProvider(database));
            }
        };
    }
}
