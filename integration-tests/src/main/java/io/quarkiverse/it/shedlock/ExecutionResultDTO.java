package io.quarkiverse.it.shedlock;

import java.util.Objects;

import net.javacrumbs.shedlock.core.LockingTaskExecutor.TaskResult;

public record ExecutionResultDTO(Boolean executed, Integer result) {
    public ExecutionResultDTO {
        Objects.requireNonNull(executed);
    }

    public ExecutionResultDTO(final TaskResult<Integer> taskResult) {
        this(taskResult.wasExecuted(), taskResult.getResult());
    }
}
