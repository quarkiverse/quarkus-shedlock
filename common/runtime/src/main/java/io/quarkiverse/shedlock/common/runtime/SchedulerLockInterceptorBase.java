package io.quarkiverse.shedlock.common.runtime;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import jakarta.interceptor.InvocationContext;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

public abstract class SchedulerLockInterceptorBase {
    public static final String SHED_LOCK = "shedLock";
    private final ShedLockConfiguration shedLockConfiguration;
    private final InstantProvider instantProvider;

    protected SchedulerLockInterceptorBase(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider) {
        this.shedLockConfiguration = Objects.requireNonNull(shedLockConfiguration);
        this.instantProvider = Objects.requireNonNull(instantProvider);
    }

    protected Object lock(final InvocationContext context) throws Throwable {
        // https://github.com/lukas-krecan/ShedLock/blob/master/cdi/shedlock-cdi/src/main/java/net/javacrumbs/shedlock/cdi/internal/SchedulerLockInterceptor.java#L43
        final Class<?> returnType = context.getMethod().getReturnType();
        if (!void.class.equals(returnType) && !Void.class.equals(returnType)) {
            throw new LockingNotSupportedException();
        }
        final LockingTaskExecutor lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider(context.getMethod()));
        final LockDuration lockDuration = lockDuration(context.getMethod());
        final LockConfiguration lockConfiguration = new LockConfiguration(
                instantProvider.now(),
                determineLockName(context.getMethod()),
                getValue(lockDuration.lockAtMostFor(), parseDuration(shedLockConfiguration.defaultsLockAtMostFor())),
                getValue(lockDuration.lockAtLeastFor(), shedLockConfiguration.defaultsLockAtLeastFor()
                        .map(this::parseDuration).orElse(Duration.ZERO)));
        return lockingTaskExecutor.executeWithLock(context::proceed, lockConfiguration);
    }

    protected abstract LockProvider lockProvider(Method method);

    protected abstract LockDuration lockDuration(Method method);

    private Duration getValue(final String stringValueFromAnnotation, final Duration defaultValue) {
        if (!stringValueFromAnnotation.isEmpty()) {
            return parseDuration(stringValueFromAnnotation);
        } else {
            return defaultValue;
        }
    }

    private Duration parseDuration(final String durationAsString) {
        try {
            return Duration.parse(durationAsString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String determineLockName(final Method method) {
        return method.getDeclaringClass().getName() + "_" + method.getName();
    }
}
