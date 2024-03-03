package io.quarkiverse.shedlock.providers.jdbc.deployment;

import java.util.List;

import org.jboss.jandex.DotName;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcConfig;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcLockProviderInitializer;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLockInterceptor;
import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem.ValidationErrorBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class JdbcSchedulerLockProcessor {

    private static final String FEATURE = "jdbcSchedulerLock";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceBeans(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(JdbcLockProviderInitializer.class, JdbcSchedulerLockInterceptor.class)
                        .build());
    }

    @BuildStep
    List<ValidationErrorBuildItem> validateDataSourcesDefinitionsWhenJdbcShedLockIsUsed(
            final ApplicationIndexBuildItem applicationIndexBuildItem,
            final JdbcConfig jdbcConfig,
            final List<JdbcDataSourceBuildItem> jdbcDataSourceBuildItems) {
        final DotName jdbcSchedulerLock = DotName.createSimple(JdbcSchedulerLock.class);
        final boolean shouldCheckDataSourcesBecauseLockIsUsed = applicationIndexBuildItem.getIndex()
                .getKnownClasses().stream()
                .anyMatch(classInfo -> classInfo.hasAnnotation(jdbcSchedulerLock) || classInfo.methods().stream()
                        .anyMatch(methodInfo -> methodInfo.hasAnnotation(jdbcSchedulerLock)));
        if (shouldCheckDataSourcesBecauseLockIsUsed) {
            return jdbcConfig.dataSources()
                    .keySet()
                    .stream()
                    .filter(shedLockDataSourceName -> jdbcDataSourceBuildItems.stream().map(JdbcDataSourceBuildItem::getName)
                            .noneMatch(jdbcDataSourceName -> jdbcDataSourceName.equals(shedLockDataSourceName)))
                    .map(missingDataSource -> new ValidationErrorBuildItem(
                            new IllegalStateException(String.format(
                                    "A missing datasource '%s' has been defined for ShedLock. Please fixe the ShedLock configuration or add the datasource",
                                    missingDataSource))))
                    .toList();
        } else {
            return List.of();
        }
    }
}
