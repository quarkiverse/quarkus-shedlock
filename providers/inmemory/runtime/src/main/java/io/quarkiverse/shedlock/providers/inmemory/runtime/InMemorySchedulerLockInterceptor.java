package io.quarkiverse.shedlock.providers.inmemory.runtime;

import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.quarkiverse.shedlock.common.runtime.InstantProvider;
import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkiverse.shedlock.common.runtime.ShedLockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider;

@InMemorySchedulerLock
@Priority(3001)
@Interceptor
public class InMemorySchedulerLockInterceptor extends SchedulerLockInterceptorBase {
    private final InMemoryLockProvider inMemoryLockProvider;

    public InMemorySchedulerLockInterceptor(final ShedLockConfiguration shedLockConfiguration,
            final InstantProvider instantProvider) {
        super(shedLockConfiguration, instantProvider);
        this.inMemoryLockProvider = new InMemoryLockProvider();
    }

    @Override
    @AroundInvoke
    protected Object lock(final InvocationContext context) throws Throwable {
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
