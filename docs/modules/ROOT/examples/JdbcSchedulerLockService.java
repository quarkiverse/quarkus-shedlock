import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;

@ApplicationScoped
public class JdbcSchedulerLockService {
    @JdbcSchedulerLock
    public void runUsingLock() {
        // do something
    }
}
