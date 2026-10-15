package threadpooling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import threadpooling.schedule.LeastLoadedSchedulingStrategy;
import threadpooling.schedule.RoundRobinSchedulingStrategy;
import threadpooling.schedule.TaskSchedulingStrategy;

/**
 * Demonstrates the Thread Pooling architectural tactic by executing a parallel
 * prime number search using different task scheduling strategies.
 */
public class App {

    private static final int THREAD_COUNT = 10; // Number of reusable worker threads in the pool
    private static final int TASK_COUNT = 100; // Number of independent prime-search tasks to submit
    private static final int MAX_NUMBER = 10_000_000; // Upper bound of numerical range searched for prime numbers.

    public static void main(String[] args) throws Exception {
        runSearch("Round Robin", new RoundRobinSchedulingStrategy());
        runSearch("Least Loaded", new LeastLoadedSchedulingStrategy());

    }

    private static void runSearch(String strategyName, TaskSchedulingStrategy strategy) throws Exception {
        PrimeSearch primeSearch = new PrimeSearch();
        List<Future<List<Integer>>> results = new ArrayList<>();
        int rangeSize = MAX_NUMBER / TASK_COUNT;
        long startTime = System.nanoTime();

        try (ThreadPool pool = new ThreadPool(THREAD_COUNT, strategy)) {
            for (int i = 0; i < TASK_COUNT; i++) {
                final int start = i * rangeSize + 1;
                final int end = (i == TASK_COUNT - 1) ? MAX_NUMBER : (i+1) * rangeSize;

                Future<List<Integer>> future = pool.submit(() -> primeSearch.findPrimes(start, end));

                results.add(future);
            }

            int totalPrimes = 0;

            for(Future<List<Integer>> result : results) {
                totalPrimes+= result.get().size();
            }

            long elapsedTime = System.nanoTime() - startTime;

            System.out.println("\nStrategy: " + strategyName);
            System.out.println("Threads: " + THREAD_COUNT);
            System.out.println("Tasks: " + TASK_COUNT);
            System.out.println("Prime numbers found: " + totalPrimes);
            System.out.printf("Execution time: %3f seconds%n", elapsedTime / 1_000_000_000.0);
        }
    }
}
