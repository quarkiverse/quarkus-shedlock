package io.quarkiverse.shedlock.common.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import net.javacrumbs.shedlock.cdi.internal.SchedulerLockInterceptor;

public class CommonDeployment {
    @BuildStep
    void registerInterceptor(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClasses(SchedulerLockInterceptor.class)
                .build());
    }
}
