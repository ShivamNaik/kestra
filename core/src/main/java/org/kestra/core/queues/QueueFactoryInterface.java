package org.kestra.core.queues;

import org.kestra.core.models.executions.Execution;
import org.kestra.core.models.executions.LogEntry;
import org.kestra.core.runners.WorkerTask;
import org.kestra.core.runners.WorkerTaskResult;

public interface QueueFactoryInterface {
    String EXECUTION_NAMED = "executionQueue";
    String WORKERTASK_NAMED = "workerTaskQueue";
    String WORKERTASKRESULT_NAMED = "workerTaskResultQueue";
    String WORKERTASKLOG_NAMED = "workerTaskLogQueue";

    QueueInterface<Execution> execution();

    QueueInterface<WorkerTask> workerTask();

    QueueInterface<WorkerTaskResult> workerTaskResult();

    QueueInterface<LogEntry> logEntry();
}
