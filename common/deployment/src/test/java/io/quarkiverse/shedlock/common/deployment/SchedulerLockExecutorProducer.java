package io.quarkiverse.shedlock.common.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;

public final class SchedulerLockExecutorProducer {
    @ApplicationScoped
    @Produces
    public SchedulerLockExecutor schedulerLockExecutor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final LockProvider lockProvider) {
        return new SchedulerLockExecutor(shedLockConfiguration, instantProvider, lockProvider, "", "");
    }
}
