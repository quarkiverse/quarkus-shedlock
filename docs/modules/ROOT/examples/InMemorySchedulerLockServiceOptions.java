import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;

@ApplicationScoped
public class InMemorySchedulerLockServiceOptions {
    @InMemorySchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void runUsingLock() {
        // TODO do something
    }
}
