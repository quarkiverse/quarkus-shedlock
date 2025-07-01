package io.quarkiverse.shedlock.providers.inmemory.deployment;

import jakarta.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;

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
    SyntheticBeanBuildItem registerInMemorySchedulerLockExecutor(
            final ShedLockConfiguration shedLockConfiguration,
            final InMemorySchedulerLockExecutorRecorder inMemorySchedulerLockExecutorRecorder) {
        return SyntheticBeanBuildItem.configure(SchedulerLockExecutor.class)
                .scope(Singleton.class)
                .createWith(inMemorySchedulerLockExecutorRecorder.schedulerLockExecutorSupplier(shedLockConfiguration))
                .addQualifier(AnnotationInstance.builder(InMemorySchedulerLockExecutor.class).build())
                .addInjectionPoint(ClassType.create(DotName.createSimple(DefaultInMemoryLockProvider.class)))
                .addInjectionPoint(ClassType.create(DotName.createSimple(InstantProvider.class)))
                .unremovable()
                .setRuntimeInit()
                .done();
    }

    @BuildStep
    AdditionalBeanBuildItem registerQualifier() {
        return new AdditionalBeanBuildItem(InMemorySchedulerLockExecutor.class);
    }
}
