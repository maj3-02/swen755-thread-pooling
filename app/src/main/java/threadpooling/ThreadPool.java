package threadpooling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import threadpooling.schedule.RoundRobinSchedulingStrategy;
import threadpooling.schedule.TaskSchedulingStrategy;

/**
 * A small fixed-size thread pool whose worker selection is controlled by a
 * scheduling strategy.
 */
public final class ThreadPool implements AutoCloseable {
    private static final Runnable STOP = () -> {
    };

    private final List<BlockingQueue<Runnable>> workQueues;
    private final List<Thread> workers;
    private final TaskSchedulingStrategy schedulingStrategy;
    private boolean acceptingTasks = true;

    public ThreadPool(int threadCount) {
        this(threadCount, new RoundRobinSchedulingStrategy());
    }

    public ThreadPool(int threadCount, TaskSchedulingStrategy schedulingStrategy) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be greater than zero.");
        }
        this.schedulingStrategy = Objects.requireNonNull(
                schedulingStrategy,
                "Scheduling strategy must not be null.");

        workQueues = new ArrayList<>(threadCount);
        workers = new ArrayList<>(threadCount);
        for (int workerNumber = 0; workerNumber < threadCount; workerNumber++) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
            Thread worker = new Thread(
                    () -> runTasks(queue),
                    "thread-pool-worker-" + workerNumber);
            workQueues.add(queue);
            workers.add(worker);
            worker.start();
        }
    }

    /** Assigns a task to the worker selected by this pool's strategy. */
    public synchronized <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException("Task must not be null.");
        }
        if (!acceptingTasks) {
            throw new IllegalStateException("Thread pool is closed.");
        }

        FutureTask<T> future = new FutureTask<>(task);
        List<Integer> queuedTaskCounts = workQueues.stream()
                .map(BlockingQueue::size)
                .toList();
        int workerIndex = schedulingStrategy.selectWorker(queuedTaskCounts);
        if (workerIndex < 0 || workerIndex >= workers.size()) {
            throw new IllegalStateException(
                    "Scheduling strategy selected invalid worker index: " + workerIndex);
        }
        workQueues.get(workerIndex).add(future);
        return future;
    }

    private void runTasks(BlockingQueue<Runnable> queue) {
        try {
            while (true) {
                Runnable task = queue.take();
                if (task == STOP) {
                    return;
                }
                task.run();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /** Stops accepting work, finishes queued tasks, and waits for all workers. */
    @Override
    public void close() {
        synchronized (this) {
            if (!acceptingTasks) {
                return;
            }
            acceptingTasks = false;
            workQueues.forEach(queue -> queue.add(STOP));
        }

        boolean interrupted = false;
        for (Thread worker : workers) {
            while (worker.isAlive()) {
                try {
                    worker.join();
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
