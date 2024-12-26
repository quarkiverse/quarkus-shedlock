package io.quarkiverse.it.shedlock;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockExecutor;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLock;
import io.quarkiverse.shedlock.providers.inmemory.runtime.InMemorySchedulerLockExecutor;
import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;

@ApplicationScoped
@Path("/inMemoryStorageLockableResource")
public class InMemoryStorageLockableResource extends AbstractLockableResource {

    final SchedulerLockExecutor schedulerLockExecutor;

    public InMemoryStorageLockableResource(
            @InMemorySchedulerLockExecutor(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S") final SchedulerLockExecutor schedulerLockExecutor) {
        this.schedulerLockExecutor = Objects.requireNonNull(schedulerLockExecutor);
    }

    @POST
    @Path("interceptor")
    @InMemorySchedulerLock(lockDuration = @LockDuration(lockAtMostFor = "PT30S", lockAtLeastFor = "PT10S"))
    public void doSomethingUsingInMemoryStorageForLock() {
        doSomething();
    }

    @POST
    @Path("execute")
    @Produces(MediaType.APPLICATION_JSON)
    public ExecutionResultDTO executeSomethingUsingInMemoryStorageForLock() {
        final TaskResult<Integer> result = schedulerLockExecutor.executeWithLock(this::doSomething, "counterInMemory");
        return new ExecutionResultDTO(result);
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
