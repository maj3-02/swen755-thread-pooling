package threadpooling;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.junit.Test;

import threadpooling.schedule.TaskSchedulingStrategy;

public class ThreadPoolTest {
    @Test
    public void distributesTasksEvenlyAcrossWorkers() throws Exception {
        List<Future<String>> results = new ArrayList<>();

        try (ThreadPool pool = new ThreadPool(3)) {
            for (int task = 0; task < 8; task++) {
                results.add(pool.submit(() -> Thread.currentThread().getName()));
            }

            Map<String, Integer> tasksPerWorker = new HashMap<>();
            for (Future<String> result : results) {
                tasksPerWorker.merge(result.get(), 1, Integer::sum);
            }

            assertEquals(3, tasksPerWorker.size());
            assertEquals(List.of(2, 3, 3), tasksPerWorker.values().stream().sorted().toList());
        }
    }

    @Test
    public void usesTheProvidedSchedulingStrategy() throws Exception {
        TaskSchedulingStrategy secondWorkerOnly = queuedTaskCounts -> 1;

        try (ThreadPool pool = new ThreadPool(2, secondWorkerOnly)) {
            Future<String> first = pool.submit(() -> Thread.currentThread().getName());
            Future<String> second = pool.submit(() -> Thread.currentThread().getName());

            assertEquals("thread-pool-worker-1", first.get());
            assertEquals("thread-pool-worker-1", second.get());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsAnInvalidWorkerSelectedByTheStrategy() {
        try (ThreadPool pool = new ThreadPool(2, queuedTaskCounts -> 2)) {
            pool.submit(() -> null);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveThreadCounts() {
        new ThreadPool(0);
    }
}
