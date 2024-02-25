package io.quarkiverse.shedlock.providers.jdbc.deployment;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcLockProviderInitializer;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcLockProviderProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class JdbcShedlockProcessor {

    private static final String FEATURE = "jdbcShedlock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(JdbcLockProviderProducer.class)
                        .build());
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(JdbcLockProviderInitializer.class)
                        .build());
    }
}
