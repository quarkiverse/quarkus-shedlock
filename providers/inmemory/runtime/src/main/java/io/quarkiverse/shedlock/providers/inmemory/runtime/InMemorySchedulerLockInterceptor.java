package io.quarkiverse.shedlock.providers.inmemory.runtime;

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

@InMemorySchedulerLock
@Priority(3001)
@Interceptor
public class InMemorySchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    private final DefaultInMemoryLockProvider inMemoryLockProvider;

    public InMemorySchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider,
            final DefaultInMemoryLockProvider inMemoryLockProvider) {
        super(shedLockConfiguration, instantProvider);
        this.inMemoryLockProvider = Objects.requireNonNull(inMemoryLockProvider);
    }

    @Override
    @AroundInvoke
    protected Object lock(InvocationContext context) throws Throwable {
        return super.lock(context);
    }

    @Override
    protected LockProvider lockProvider(final Method method) {
        return inMemoryLockProvider;
    }

    @Override
    protected LockDuration lockDuration(final Method method) {
        return method.getAnnotation(InMemorySchedulerLock.class).lockDuration();
    }
}
