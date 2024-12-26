package io.quarkiverse.shedlock.providers.mongo.reactive.deployment.deployment;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveConfig;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLockExecutorRecorder;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLockInterceptor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class MongoReactiveSchedulerLockProcessor {

    private static final String FEATURE = "mongoReactiveSchedulerLock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(MongoReactiveSchedulerLockInterceptor.class)
                        .build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    List<SyntheticBeanBuildItem> registerJdbcSchedulerLockExecutors(
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final ShedLockConfiguration shedLockConfiguration,
            final MongoReactiveConfig mongoReactiveConfig,
            final MongoReactiveSchedulerLockExecutorRecorder mongoReactiveSchedulerLockExecutorRecorder) {
        final IndexView index = combinedIndexBuildItem.getComputingIndex();
        final Set<Qualifier> qualifiers = combinedIndexBuildItem.getIndex()
                .getAnnotations(DotName.createSimple(MongoReactiveSchedulerLockExecutor.class))
                .stream()
                .map(qualifier -> {
                    final String mongoClientName = qualifier.valueWithDefault(index, "mongoClientName").asString();
                    final String lockAtMostFor = qualifier.valueWithDefault(index, "lockAtMostFor").asString();
                    final String lockAtLeastFor = qualifier.valueWithDefault(index, "lockAtLeastFor").asString();
                    return new Qualifier(mongoClientName, lockAtMostFor, lockAtLeastFor);
                })
                .collect(Collectors.toSet());

        return qualifiers.stream()
                .map(qualifier -> SyntheticBeanBuildItem.configure(SchedulerLockExecutor.class)
                        .scope(Singleton.class)
                        .createWith(
                                mongoReactiveSchedulerLockExecutorRecorder.schedulerLockExecutorSupplier(shedLockConfiguration,
                                        mongoReactiveConfig,
                                        qualifier.mongoClientName,
                                        qualifier.lockAtMostFor,
                                        qualifier.lockAtLeastFor))
                        .addQualifier(qualifier.toQualifier())
                        .addInjectionPoint(ClassType.create(DotName.createSimple(InstantProvider.class)))
                        .unremovable()
                        .setRuntimeInit()
                        .done())
                .toList();
    }

    // https://github.com/quarkusio/quarkus/issues/45289
    @BuildStep
    AdditionalBeanBuildItem registerQualifier() {
        return new AdditionalBeanBuildItem(MongoReactiveSchedulerLockExecutor.class);
    }

    record Qualifier(String mongoClientName, String lockAtMostFor, String lockAtLeastFor) {
        Qualifier {
            Objects.requireNonNull(mongoClientName);
            Objects.requireNonNull(lockAtMostFor);
            Objects.requireNonNull(lockAtLeastFor);
        }

        AnnotationInstance toQualifier() {
            return AnnotationInstance.builder(MongoReactiveSchedulerLockExecutor.class)
                    .add("mongoClientName", mongoClientName)
                    .add("lockAtMostFor", lockAtMostFor)
                    .add("lockAtLeastFor", lockAtLeastFor)
                    .build();
        }
    }
}
