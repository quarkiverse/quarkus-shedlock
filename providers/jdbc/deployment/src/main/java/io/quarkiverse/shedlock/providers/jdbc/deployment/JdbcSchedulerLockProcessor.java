package io.quarkiverse.shedlock.providers.jdbc.deployment;

import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Singleton;

import org.jboss.jandex.DotName;

import io.quarkiverse.shedlock.providers.jdbc.runtime.*;
import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem.ValidationErrorBuildItem;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
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
            final List<JdbcDataSourceBuildItem> jdbcDataSourceBuildItems) {
        final List<String> dataSourceNames = getDataSourcesNameFromJdbcSchedulerLocks(applicationIndexBuildItem);
        return dataSourceNames
                .stream()
                .filter(shedLockDataSourceName -> jdbcDataSourceBuildItems.stream().map(JdbcDataSourceBuildItem::getName)
                        .noneMatch(jdbcDataSourceName -> jdbcDataSourceName.equals(shedLockDataSourceName)))
                .map(missingDataSource -> new ValidationErrorBuildItem(
                        new IllegalStateException(String.format(
                                "A missing datasource '%s' has been defined for ShedLock. Please fixe the ShedLock configuration or add the datasource",
                                missingDataSource))))
                .toList();
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem registerJdbcLockProviderInitializer(
            final ApplicationIndexBuildItem applicationIndexBuildItem,
            final DataSourcesRecorder dataSourcesRecorder) {
        final List<DataSourceName> dataSourceNames = getDataSourcesNameFromJdbcSchedulerLocks(applicationIndexBuildItem)
                .stream().map(DataSourceName::new)
                .toList();
        return SyntheticBeanBuildItem.configure(DataSources.class)
                .scope(Singleton.class)
                .supplier(dataSourcesRecorder.dataSourcesSupplier(dataSourceNames))
                .unremovable()
                .done();
    }

    private List<String> getDataSourcesNameFromJdbcSchedulerLocks(ApplicationIndexBuildItem applicationIndexBuildItem) {
        final DotName jdbcSchedulerLock = DotName.createSimple(JdbcSchedulerLock.class);
        return Stream.concat(
                applicationIndexBuildItem.getIndex().getKnownClasses()
                        .stream().filter(classInfo -> classInfo.hasAnnotation(jdbcSchedulerLock))
                        .map(classInfo -> classInfo.annotation(jdbcSchedulerLock)),
                applicationIndexBuildItem.getIndex().getKnownClasses().stream()
                        .flatMap(classInfo -> classInfo.methods().stream())
                        .filter(methodInfo -> methodInfo.hasAnnotation(jdbcSchedulerLock))
                        .map(methodInfo -> methodInfo.annotation(jdbcSchedulerLock)))
                .map(annotationInstance -> annotationInstance.value("dataSourceName"))
                .map(dataSourceName -> dataSourceName != null ? dataSourceName.asString()
                        : DataSourceUtil.DEFAULT_DATASOURCE_NAME)
                .distinct()
                .toList();
    }
}
