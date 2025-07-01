import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime.MongoReactiveSchedulerLock;

@ApplicationScoped
public class MongoReactiveSchedulerLockService {
    @MongoReactiveSchedulerLock
    public void runUsingLock() {
        // do something
    }
}
