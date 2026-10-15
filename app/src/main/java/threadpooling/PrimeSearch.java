package threadpooling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** Finds prime numbers without sharing mutable state between searches. */
public class PrimeSearch {

    private boolean isPrime(int number) {
        if (number < 2) {
            return false;
        }
        if (number == 2) {
            return true;
        }
        if (number % 2 == 0) {
            return false;
        }

        // Equivalent to divisor * divisor <= number, without integer overflow.
        for (int divisor = 3; divisor <= number / divisor; divisor += 2) {
            if (number % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the primes in ascending order, including both range endpoints.
     * Each call returns a new list and can run concurrently with other calls.
     *
     * @throws IllegalArgumentException if the start exceeds the end
     */
    public List<Integer> findPrimes(int startInclusive, int endInclusive) {
        if (startInclusive > endInclusive) {
            throw new IllegalArgumentException("Start of range must not exceed end of range.");
        }

        List<Integer> primes = new ArrayList<>();
        // A long loop counter avoids wrapping after Integer.MAX_VALUE.
        for (long candidate = Math.max(2, startInclusive); candidate <= endInclusive; candidate++) {
            int number = (int) candidate;
            if (isPrime(number)) {
                primes.add(number);
            }
        }
        return primes;
    }

    /**
     * Returns the primes in ascending order using evenly sized range tasks.
     * When the range cannot divide exactly, earlier workers receive one extra
     * candidate. No more workers are created than there are candidates.
     *
     * @throws IllegalArgumentException if the range is reversed or threadCount
     *                                  is not positive
     */
    public List<Integer> findPrimes(int startInclusive, int endInclusive, int threadCount) {
        if (startInclusive > endInclusive) {
            throw new IllegalArgumentException("Start of range must not exceed end of range.");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be greater than zero.");
        }

        long firstCandidate = Math.max(2L, startInclusive);
        long candidateCount = (long) endInclusive - firstCandidate + 1;
        if (candidateCount <= 0) {
            return new ArrayList<>();
        }

        int workerCount = (int) Math.min(candidateCount, threadCount);
        long baseTaskSize = candidateCount / workerCount;
        long remainder = candidateCount % workerCount;
        List<Future<List<Integer>>> results = new ArrayList<>(workerCount);

        try (ThreadPool pool = new ThreadPool(workerCount)) {
            long taskStart = firstCandidate;
            for (int worker = 0; worker < workerCount; worker++) {
                long taskSize = baseTaskSize + (worker < remainder ? 1 : 0);
                int rangeStart = (int) taskStart;
                int rangeEnd = (int) (taskStart + taskSize - 1);
                results.add(pool.submit(() -> findPrimes(rangeStart, rangeEnd)));
                taskStart += taskSize;
            }

            List<Integer> primes = new ArrayList<>();
            for (Future<List<Integer>> result : results) {
                primes.addAll(getResult(result));
            }
            return primes;
        }
    }

    private List<Integer> getResult(Future<List<Integer>> result) {
        try {
            return result.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Prime search was interrupted.", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Prime search task failed.", cause);
        }
    }
}
