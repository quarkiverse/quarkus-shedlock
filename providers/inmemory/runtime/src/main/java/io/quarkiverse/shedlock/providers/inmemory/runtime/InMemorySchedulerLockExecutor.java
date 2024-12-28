package io.quarkiverse.shedlock.providers.inmemory.runtime;

import java.lang.annotation.*;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface InMemorySchedulerLockExecutor {
}
