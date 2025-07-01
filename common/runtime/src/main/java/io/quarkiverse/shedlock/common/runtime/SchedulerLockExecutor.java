package io.quarkiverse.shedlock.common.runtime;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskWithResult;
import net.javacrumbs.shedlock.support.LockException;

public final class SchedulerLockExecutor {

    private final ShedLockConfiguration shedLockConfiguration;
    private final InstantProvider instantProvider;
    private final LockProvider lockProvider;

    public SchedulerLockExecutor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final LockProvider lockProvider) {
        this.shedLockConfiguration = Objects.requireNonNull(shedLockConfiguration);
        this.instantProvider = Objects.requireNonNull(instantProvider);
        this.lockProvider = Objects.requireNonNull(lockProvider);
    }

    public <T> TaskResult<T> executeWithLock(final TaskWithResult<T> taskWithResult, final String lockName,
            final Duration lockAtMostFor, final Duration lockAtLeastFor)
            throws LockException {
        Objects.requireNonNull(taskWithResult, "taskWithResult is null");
        Objects.requireNonNull(lockName, "lockName is null");
        final LockingTaskExecutor lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        final LockConfiguration lockConfiguration = new LockConfiguration(
                instantProvider.now(),
                lockName,
                getValue(lockAtMostFor, parseDuration(shedLockConfiguration.defaultsLockAtMostFor())),
                getValue(lockAtLeastFor, shedLockConfiguration.defaultsLockAtLeastFor()
                        .map(SchedulerLockExecutor::parseDuration).orElse(Duration.ZERO)));
        try {
            return lockingTaskExecutor.executeWithLock(taskWithResult, lockConfiguration);
        } catch (final Throwable e) {
            throw new LockException(e.getMessage(), e);
        }
    }

    private static Duration getValue(final Duration value, final Duration defaultValue) {
        return value != null ? value : defaultValue;
    }

    private static Duration parseDuration(final String durationAsString) {
        try {
            return Duration.parse(durationAsString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
