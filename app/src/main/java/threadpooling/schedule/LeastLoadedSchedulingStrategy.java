package threadpooling.schedule;

import java.util.List;

/**
 * Assigns each task to the worker with the fewest tasks waiting in its queue.
 */
public final class LeastLoadedSchedulingStrategy implements TaskSchedulingStrategy{
    @Override
    public int selectWorker(List<Integer> queuedTaskCounts) {
        if (queuedTaskCounts.isEmpty()) {
            throw new IllegalArgumentException("At least one worker is required");
        }

        int selectedWorker = 0;
        int minimumTasks = queuedTaskCounts.get(0);

        for (int i = 1; i < queuedTaskCounts.size(); i++) {
            int currentTasks = queuedTaskCounts.get(i);

            if (currentTasks < minimumTasks) {
                minimumTasks = currentTasks;
                selectedWorker = i;
            }
        }
        return selectedWorker;
    }
}
