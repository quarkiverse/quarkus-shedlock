package io.quarkiverse.shedlock.common.deployment;

import io.quarkiverse.shedlock.common.runtime.DefaultInstantProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class CommonProcessor {
    @BuildStep
    void registerBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClasses(DefaultInstantProvider.class)
                .build());
    }
}
