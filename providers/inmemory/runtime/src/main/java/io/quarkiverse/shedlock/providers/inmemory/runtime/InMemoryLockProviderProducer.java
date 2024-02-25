package io.quarkiverse.shedlock.providers.inmemory.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider;

public class InMemoryLockProviderProducer {
    @ApplicationScoped
    @Produces
    public LockProvider lockProvider() {
        return new InMemoryLockProvider();
    }
}
