package io.quarkiverse.shedlock.providers.inmemory.runtime;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.DefaultBean;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider;

@ApplicationScoped
@DefaultBean
public class DefaultInMemoryLockProvider implements LockProvider {
    private final InMemoryLockProvider inMemoryLockProvider;

    public DefaultInMemoryLockProvider() {
        this.inMemoryLockProvider = new InMemoryLockProvider();
    }

    @Override
    public Optional<SimpleLock> lock(final LockConfiguration lockConfiguration) {
        return inMemoryLockProvider.lock(lockConfiguration);
    }
}
