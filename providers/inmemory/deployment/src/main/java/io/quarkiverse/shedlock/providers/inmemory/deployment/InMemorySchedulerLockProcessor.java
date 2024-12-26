package io.quarkiverse.shedlock.providers.inmemory.deployment;

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
import io.quarkiverse.shedlock.providers.inmemory.runtime.DefaultInMemoryLockProvider;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockExecutorRecorder;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockInterceptor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class InMemorySchedulerLockProcessor {

    private static final String FEATURE = "inMemorySchedulerLock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(InMemorySchedulerLockInterceptor.class, DefaultInMemoryLockProvider.class)
                        .build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    List<SyntheticBeanBuildItem> registerInMemorySchedulerLockExecutors(
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final ShedLockConfiguration shedLockConfiguration,
            final InMemorySchedulerLockExecutorRecorder inMemorySchedulerLockExecutorRecorder) {
        final IndexView index = combinedIndexBuildItem.getComputingIndex();
        final Set<Qualifier> qualifiers = combinedIndexBuildItem.getIndex()
                .getAnnotations(DotName.createSimple(InMemorySchedulerLockExecutor.class))
                .stream()
                .map(qualifier -> {
                    final String lockAtMostFor = qualifier.valueWithDefault(index, "lockAtMostFor").asString();
                    final String lockAtLeastFor = qualifier.valueWithDefault(index, "lockAtLeastFor").asString();
                    return new Qualifier(lockAtMostFor, lockAtLeastFor);
                })
                .collect(Collectors.toSet());

        return qualifiers.stream()
                .map(qualifier -> SyntheticBeanBuildItem.configure(SchedulerLockExecutor.class)
                        .scope(Singleton.class)
                        .createWith(inMemorySchedulerLockExecutorRecorder.schedulerLockExecutorSupplier(shedLockConfiguration,
                                qualifier.lockAtMostFor,
                                qualifier.lockAtLeastFor))
                        .addQualifier(qualifier.toQualifier())
                        .addInjectionPoint(ClassType.create(DotName.createSimple(DefaultInMemoryLockProvider.class)))
                        .addInjectionPoint(ClassType.create(DotName.createSimple(InstantProvider.class)))
                        .unremovable()
                        .setRuntimeInit()
                        .done())
                .toList();
    }

    // https://github.com/quarkusio/quarkus/issues/45289
    @BuildStep
    AdditionalBeanBuildItem registerQualifier() {
        return new AdditionalBeanBuildItem(InMemorySchedulerLockExecutor.class);
    }

    record Qualifier(String lockAtMostFor, String lockAtLeastFor) {
        Qualifier {
            Objects.requireNonNull(lockAtMostFor);
            Objects.requireNonNull(lockAtLeastFor);
        }

        AnnotationInstance toQualifier() {
            return AnnotationInstance.builder(InMemorySchedulerLockExecutor.class)
                    .add("lockAtMostFor", lockAtMostFor)
                    .add("lockAtLeastFor", lockAtLeastFor)
                    .build();
        }
    }
}
