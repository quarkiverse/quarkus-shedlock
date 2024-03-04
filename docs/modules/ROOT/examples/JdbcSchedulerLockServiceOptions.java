import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.providers.jdbc.runtime.JdbcSchedulerLock;

@ApplicationScoped
public class JdbcSchedulerLockServiceOptions {
    @JdbcSchedulerLock(dataSourceName = "master", lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void runUsingLock() {
        // do something
    }
}
