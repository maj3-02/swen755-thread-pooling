package threadpooling;

import java.util.ArrayList;
import java.util.List;

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
}
