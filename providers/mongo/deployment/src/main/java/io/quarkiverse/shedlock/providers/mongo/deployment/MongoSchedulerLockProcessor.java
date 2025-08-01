package io.quarkiverse.shedlock.providers.mongo.deployment;

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
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoConfig;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLockExecutorRecorder;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLockInterceptor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class MongoSchedulerLockProcessor {

    private static final String FEATURE = "mongoSchedulerLock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(MongoSchedulerLockInterceptor.class)
                        .build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    List<SyntheticBeanBuildItem> registerMongoSchedulerLockExecutors(
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final ShedLockConfiguration shedLockConfiguration,
            final MongoConfig mongoConfig,
            final MongoSchedulerLockExecutorRecorder mongoSchedulerLockExecutorRecorder) {
        final IndexView index = combinedIndexBuildItem.getComputingIndex();
        final Set<Qualifier> qualifiers = combinedIndexBuildItem.getIndex()
                .getAnnotations(DotName.createSimple(MongoSchedulerLockExecutor.class))
                .stream()
                .map(qualifier -> {
                    final String mongoClientName = qualifier.valueWithDefault(index, "mongoClientName").asString();
                    return new Qualifier(mongoClientName);
                })
                .collect(Collectors.toSet());

        return qualifiers.stream()
                .map(qualifier -> SyntheticBeanBuildItem.configure(SchedulerLockExecutor.class)
                        .scope(Singleton.class)
                        .createWith(mongoSchedulerLockExecutorRecorder.schedulerLockExecutorSupplier(shedLockConfiguration,
                                mongoConfig,
                                qualifier.mongoClientName))
                        .addQualifier(qualifier.toQualifier())
                        .addInjectionPoint(ClassType.create(DotName.createSimple(InstantProvider.class)))
                        .unremovable()
                        .setRuntimeInit()
                        .done())
                .toList();
    }

    @BuildStep
    AdditionalBeanBuildItem registerQualifier() {
        return new AdditionalBeanBuildItem(MongoSchedulerLockExecutor.class);
    }

    record Qualifier(String mongoClientName) {
        Qualifier {
            Objects.requireNonNull(mongoClientName);
        }

        AnnotationInstance toQualifier() {
            return AnnotationInstance.builder(MongoSchedulerLockExecutor.class)
                    .add("mongoClientName", mongoClientName)
                    .build();
        }
    }
}
