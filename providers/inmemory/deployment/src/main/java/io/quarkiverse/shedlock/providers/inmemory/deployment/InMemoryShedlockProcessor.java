package io.quarkiverse.shedlock.providers.inmemory.deployment;

import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemoryLockProviderProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class InMemoryShedlockProcessor {

    private static final String FEATURE = "inMemoryShedlock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(InMemoryLockProviderProducer.class)
                        .build());
    }
}
