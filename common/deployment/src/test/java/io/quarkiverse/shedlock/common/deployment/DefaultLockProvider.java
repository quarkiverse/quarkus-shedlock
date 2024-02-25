package io.quarkiverse.shedlock.common.deployment;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;

@ApplicationScoped
public class DefaultLockProvider implements LockProvider {
    private boolean called = false;

    @Override
    public Optional<SimpleLock> lock(final LockConfiguration lockConfiguration) {
        this.called = true;
        return Optional.empty();
    }

    public boolean isCalled() {
        return called;
    }
}
