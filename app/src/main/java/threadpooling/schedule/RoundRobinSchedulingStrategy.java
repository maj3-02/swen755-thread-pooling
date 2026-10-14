package threadpooling.schedule;

import java.util.List;

/** Assigns tasks to each worker in turn for an even task-count distribution. */
public final class RoundRobinSchedulingStrategy implements TaskSchedulingStrategy {
    private int nextWorker;

    @Override
    public int selectWorker(List<Integer> queuedTaskCounts) {
        if (queuedTaskCounts.isEmpty()) {
            throw new IllegalArgumentException("At least one worker is required.");
        }

        int selectedWorker = nextWorker;
        nextWorker = (nextWorker + 1) % queuedTaskCounts.size();
        return selectedWorker;
    }
}
