import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;

@ApplicationScoped
public class MongoReactiveSchedulerLockServiceOptions {
    @MongoReactiveSchedulerLock(mongoClientName = "cluster1reactive", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void runUsingLock() {
        // do something
    }
}
