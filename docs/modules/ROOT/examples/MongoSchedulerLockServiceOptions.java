import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@ApplicationScoped
public class MongoSchedulerLockServiceOptions {
    @MongoSchedulerLock(mongoClientName = "cluster1", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void runUsingLock() {
        // do something
    }
}
