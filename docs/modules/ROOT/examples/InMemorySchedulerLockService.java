import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;

@ApplicationScoped
public class InMemorySchedulerLockService {
    @InMemorySchedulerLock
    public void runUsingLock() {
        // do something
    }
}
