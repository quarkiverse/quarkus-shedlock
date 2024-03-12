package io.quarkiverse.shedlock.common.deployment;

import java.lang.reflect.Method;
import java.util.Objects;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;

@DefaultSchedulerLock
@Priority(3001)
@Interceptor
public class DefaultSchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    final DefaultLockProvider lockProvider;

    protected DefaultSchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final DefaultLockProvider lockProvider) {
        super(shedLockConfiguration, instantProvider);
        this.lockProvider = Objects.requireNonNull(lockProvider);
    }

    @Override
    @AroundInvoke
    protected Object lock(InvocationContext context) throws Throwable {
        return super.lock(context);
    }

    @Override
    protected LockProvider lockProvider(Method method) {
        return lockProvider;
    }

    @Override
    protected LockDuration lockDuration(Method method) {
        return method.getAnnotation(DefaultSchedulerLock.class).lockDuration();
    }
}
