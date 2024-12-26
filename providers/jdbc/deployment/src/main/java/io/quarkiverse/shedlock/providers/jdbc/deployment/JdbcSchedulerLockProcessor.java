package io.quarkiverse.shedlock.providers.jdbc.deployment;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
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
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
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
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final List<JdbcDataSourceBuildItem> jdbcDataSourceBuildItems) {
        final List<String> dataSourceNames = getDataSourcesNameFromJdbcSchedulerLocks(combinedIndexBuildItem);
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
    List<SyntheticBeanBuildItem> registerJdbcLockProviderInitializer(
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final DataSourceNameRecorder dataSourceNameRecorder) {
        final List<String> dataSourceNames = getDataSourcesNameFromJdbcSchedulerLocks(combinedIndexBuildItem);
        return dataSourceNames.stream()
                .map(dataSourceName -> SyntheticBeanBuildItem.configure(DataSourceName.class)
                        .scope(Singleton.class)
                        .identifier(dataSourceName)
                        .supplier(dataSourceNameRecorder.dataSourceNameSupplier(dataSourceName))
                        .unremovable()
                        .done())
                .toList();
    }

    private List<String> getDataSourcesNameFromJdbcSchedulerLocks(final CombinedIndexBuildItem combinedIndexBuildItem) {
        final DotName jdbcSchedulerLock = DotName.createSimple(JdbcSchedulerLock.class);
        return Stream.concat(
                combinedIndexBuildItem.getIndex().getKnownClasses()
                        .stream().filter(classInfo -> classInfo.hasAnnotation(jdbcSchedulerLock))
                        .map(classInfo -> classInfo.annotation(jdbcSchedulerLock)),
                combinedIndexBuildItem.getIndex().getKnownClasses().stream()
                        .flatMap(classInfo -> classInfo.methods().stream())
                        .filter(methodInfo -> methodInfo.hasAnnotation(jdbcSchedulerLock))
                        .map(methodInfo -> methodInfo.annotation(jdbcSchedulerLock)))
                .map(annotationInstance -> annotationInstance.value("dataSourceName"))
                .map(dataSourceName -> dataSourceName != null ? dataSourceName.asString()
                        : DataSourceUtil.DEFAULT_DATASOURCE_NAME)
                .distinct()
                .toList();
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    List<SyntheticBeanBuildItem> registerJdbcSchedulerLockExecutors(
            final CombinedIndexBuildItem combinedIndexBuildItem,
            final ShedLockConfiguration shedLockConfiguration,
            final JdbcConfig jdbcConfig,
            final JdbcSchedulerLockExecutorRecorder jdbcSchedulerLockExecutorRecorder) {
        final IndexView index = combinedIndexBuildItem.getComputingIndex();
        final Set<Qualifier> qualifiers = combinedIndexBuildItem.getIndex()
                .getAnnotations(DotName.createSimple(JdbcSchedulerLockExecutor.class))
                .stream()
                .map(qualifier -> {
                    final String dataSourceName = qualifier.valueWithDefault(index, "dataSourceName").asString();
                    final String lockAtMostFor = qualifier.valueWithDefault(index, "lockAtMostFor").asString();
                    final String lockAtLeastFor = qualifier.valueWithDefault(index, "lockAtLeastFor").asString();
                    return new Qualifier(dataSourceName, lockAtMostFor, lockAtLeastFor);
                })
                .collect(Collectors.toSet());

        return qualifiers.stream()
                .map(qualifier -> SyntheticBeanBuildItem.configure(SchedulerLockExecutor.class)
                        .scope(Singleton.class)
                        .createWith(jdbcSchedulerLockExecutorRecorder.schedulerLockExecutorSupplier(shedLockConfiguration,
                                jdbcConfig,
                                qualifier.dataSourceName,
                                qualifier.lockAtMostFor,
                                qualifier.lockAtLeastFor))
                        .addQualifier(qualifier.toQualifier())
                        .addInjectionPoint(ClassType.create(DotName.createSimple(InstantProvider.class)))
                        .unremovable()
                        .setRuntimeInit()
                        .done())
                .toList();
    }

    // https://github.com/quarkusio/quarkus/issues/45289
    @BuildStep
    AdditionalBeanBuildItem registerQualifier() {
        return new AdditionalBeanBuildItem(JdbcSchedulerLockExecutor.class);
    }

    record Qualifier(String dataSourceName, String lockAtMostFor, String lockAtLeastFor) {
        Qualifier {
            Objects.requireNonNull(dataSourceName);
            Objects.requireNonNull(lockAtMostFor);
            Objects.requireNonNull(lockAtLeastFor);
        }

        AnnotationInstance toQualifier() {
            return AnnotationInstance.builder(JdbcSchedulerLockExecutor.class)
                    .add("dataSourceName", dataSourceName)
                    .add("lockAtMostFor", lockAtMostFor)
                    .add("lockAtLeastFor", lockAtLeastFor)
                    .build();
        }
    }
}
