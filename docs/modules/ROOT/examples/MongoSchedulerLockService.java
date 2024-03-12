import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.mongo.runtime.MongoSchedulerLock;

@ApplicationScoped
public class MongoSchedulerLockService {
    @MongoSchedulerLock
    public void runUsingLock() {
        // do something
    }
}
