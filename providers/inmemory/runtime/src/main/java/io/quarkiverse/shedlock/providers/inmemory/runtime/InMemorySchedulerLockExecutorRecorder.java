package io.quarkiverse.shedlock.providers.inmemory.runtime;

import java.util.function.Function;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class InMemorySchedulerLockExecutorRecorder {

    public Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor> schedulerLockExecutorSupplier(
            final ShedLockConfiguration shedLockConfiguration) {
        return new Function<SyntheticCreationalContext<SchedulerLockExecutor>, SchedulerLockExecutor>() {
            @Override
            public SchedulerLockExecutor apply(final SyntheticCreationalContext<SchedulerLockExecutor> context) {
                return new SchedulerLockExecutor(
                        shedLockConfiguration,
                        context.getInjectedReference(InstantProvider.class),
                        context.getInjectedReference(DefaultInMemoryLockProvider.class));
            }
        };
    }
}
