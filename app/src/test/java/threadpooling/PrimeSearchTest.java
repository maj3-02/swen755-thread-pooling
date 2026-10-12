package threadpooling;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class PrimeSearchTest {
    private final PrimeSearch search = new PrimeSearch();
    
    /**
     * This test ensures that the findPrimes method correctly identifies prime numbers within a small range.
     * It verifies that the method returns a list containing the prime numbers 2, 3, 5, and 7 when searching for primes between 1 and 10.
     */
    @Test
    public void findsPrimesInASmallRange() {
        assertEquals(List.of(2, 3, 5, 7), search.findPrimes(1, 10));
    }
    
    /**
     * This test ensures that the findPrimes method correctly includes both endpoints of the range when they are prime numbers.
     * It verifies that the method returns a list containing both 11 and 13 when searching for primes between 11 and 13.
     */
    @Test
    public void includesBothEndpoints() {
        assertEquals(List.of(11, 13), search.findPrimes(11, 13));
    }
    
    /**
     * This test ensures that the findPrimes method correctly ignores numbers below 2, as they are not prime.
     * It verifies that the method returns an empty list for ranges that do not include any prime numbers.
     */
    @Test
    public void ignoresNumbersBelowTwo() {
        assertEquals(List.of(), search.findPrimes(-10, 1));
        assertEquals(List.of(2, 3), search.findPrimes(-10, 3));
    }
    
    /**
     * This test ensures that the findPrimes method correctly handles ranges that consist of a single number.
     * It verifies that the method returns the number itself if it is prime, or an empty list if it is not.
     */
    @Test
    public void handlesSingleNumberRanges() {
        assertEquals(List.of(2), search.findPrimes(2, 2));
        assertEquals(List.of(13), search.findPrimes(13, 13));
        assertEquals(List.of(), search.findPrimes(9, 9));
        assertEquals(List.of(), search.findPrimes(49, 49));
    }
    
    /**
     * This test ensures that the findPrimes method returns an empty list when there are no prime numbers in the specified range.
     * It verifies that the method correctly identifies ranges without primes and returns an empty result.
     */
    @Test
    public void returnsAnEmptyListWhenNoPrimesExist() {
        assertEquals(List.of(), search.findPrimes(14, 16));
    }
    
    /**
     * This test ensures that the findPrimes method throws an IllegalArgumentException when the start of the range exceeds the end.
     * It verifies that the method correctly handles invalid input by rejecting reversed ranges.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsReversedRanges() {
        search.findPrimes(10, 1);
    }
    
    /**
     * This test ensures that the findPrimes method can handle the largest possible integer value without causing an overflow.
     * It verifies that the method correctly identifies Integer.MAX_VALUE as a prime number and returns it in the result list.
     */
    @Test(timeout = 2000)
    public void handlesTheLargestIntegerWithoutOverflow() {
        assertEquals(List.of(Integer.MAX_VALUE), search.findPrimes(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    
    /**
     * This test ensures that the findPrimes method produces the same results when searching adjacent ranges as it does when searching one combined range.
     * It verifies that the primes found in the ranges 1-10 and 11-20 are the same as those found in the range 1-20.
     */
    @Test
    public void adjacentRangesProduceTheSamePrimesAsOneCombinedRange() {
        List<Integer> combined = new ArrayList<>(search.findPrimes(1, 10));
        combined.addAll(search.findPrimes(11, 20));

        assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19), combined);
        assertEquals(search.findPrimes(1, 20), combined);
    }
    
    /**
     * This test ensures that the lists returned by the findPrimes method are independent of each other.
     * It verifies that modifying one list does not affect the other list or the results of subsequent calls to findPrimes.
     */
    @Test
    public void returnedListsAreIndependent() {
        List<Integer> first = search.findPrimes(1, 10);
        List<Integer> second = search.findPrimes(1, 10);
        first.clear();

        assertEquals(List.of(2, 3, 5, 7), second);
        assertEquals(List.of(2, 3, 5, 7), search.findPrimes(1, 10));
    }
    
    /**
     * This test ensures that the PrimeSearch class can handle concurrent searches on the same instance without any issues.
     * It uses two separate threads to perform searches in different ranges and verifies that the results are correct.
     */
    @Test(timeout = 5000)
    public void supportsConcurrentSearchesOnTheSameInstance() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        FutureTask<List<Integer>> first = new FutureTask<>(() -> {
            start.await();
            return search.findPrimes(1, 10);
        });
        FutureTask<List<Integer>> second = new FutureTask<>(() -> {
            start.await();
            return search.findPrimes(11, 20);
        });
        Thread firstWorker = new Thread(first, "prime-search-test-1");
        Thread secondWorker = new Thread(second, "prime-search-test-2");

        firstWorker.start();
        secondWorker.start();
        start.countDown();
        try {
            assertEquals(List.of(2, 3, 5, 7), first.get(2, TimeUnit.SECONDS));
            assertEquals(List.of(11, 13, 17, 19), second.get(2, TimeUnit.SECONDS));
        } finally {
            firstWorker.interrupt();
            secondWorker.interrupt();
            firstWorker.join();
            secondWorker.join();
        }
    }
}
