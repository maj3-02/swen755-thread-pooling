package threadpooling.schedule;

import java.util.List;

/** Selects the worker queue that receives the next submitted task. */
@FunctionalInterface
public interface TaskSchedulingStrategy {
    /**
     * Returns the zero-based index of the worker that should receive a task.
     * The supplied list is an immutable snapshot containing the number of
     * queued tasks for each worker.
     */
    int selectWorker(List<Integer> queuedTaskCounts);
}
