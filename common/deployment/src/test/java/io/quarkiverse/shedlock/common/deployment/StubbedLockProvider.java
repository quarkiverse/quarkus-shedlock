package io.quarkiverse.shedlock.common.deployment;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.Validate;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;

@ApplicationScoped
public class StubbedLockProvider implements LockProvider {
    private final Set<String> locksByName = new HashSet<>();
    private final Set<String> unlocksByName = new HashSet<>();

    @Override
    public Optional<SimpleLock> lock(final LockConfiguration lockConfiguration) {
        locksByName.add(lockConfiguration.getName());
        return Optional.of(new SimpleLock() {
            @Override
            public void unlock() {
                Validate.validState(locksByName.contains(lockConfiguration.getName()));
                unlocksByName.add(lockConfiguration.getName());
            }
        });
    }

    public boolean hasBeenLocked() {
        return !locksByName.isEmpty();
    }

    public boolean hasBeenLocked(final String lockName) {
        return locksByName.contains(lockName);
    }

    public boolean hasBeenUnlocked(final String lockName) {
        return unlocksByName.contains(lockName);
    }

    public void reset() {
        locksByName.clear();
        unlocksByName.clear();
    }
}
