package io.quarkiverse.shedlock.providers.inmemory.deployment;

import io.quarkiverse.shedlock.providers.inmemory.runtime.DefaultInMemoryLockProvider;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockInterceptor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
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
}
