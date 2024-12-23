package io.quarkiverse.it.shedlock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;

@ApplicationScoped
@Path("/inMemoryStorageLockableResource")
public class InMemoryStorageLockableResource extends AbstractLockableResource {

    @POST
    @InMemorySchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingInMemoryStorageForLock() {
        doSomething();
    }

    /**
     * unable to reset in memory storage from ShedLock
     *
     * @see io.quarkiverse.shedlock.providers.inmemory.runtime.DefaultInMemoryLockProvider
     */
    @Override
    void resetLockStorage() {
    }
}
