package threadpooling.schedule;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RoundRobinSchedulingStrategyTest {
    private RoundRobinSchedulingStrategy strat;

    @Before
    public void setup() {
        strat = new RoundRobinSchedulingStrategy();
    }

    @Test
    public void testSelectWorkerEmptyQueue() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            strat.selectWorker(queuedTaskCounts);
        });

        String expectedMessage = "At least one worker is required";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testSelectWorkerOnce() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        queuedTaskCounts.add(122);
        queuedTaskCounts.add(29);

        int actual = strat.selectWorker(queuedTaskCounts);
        int expected = 0;

        assertEquals("Expected " + expected + " but was " + actual, expected, actual);
    }

    @Test
    public void testSelectWorkerTwice() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        queuedTaskCounts.add(122);
        queuedTaskCounts.add(29);

        int actualFirst = strat.selectWorker(queuedTaskCounts);
        int actualSecond = strat.selectWorker(queuedTaskCounts);
        int expectedFirst = 0;
        int expectedSecond = 1;

        assertEquals("Expected " + expectedFirst + " but was " + actualFirst, expectedFirst, actualFirst);
        assertEquals("Expected " + expectedSecond + " but was " + actualSecond, expectedSecond, actualSecond);
    }

    @Test
    public void testSelectWorkerOverflow() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        queuedTaskCounts.add(122);
        queuedTaskCounts.add(29);
        queuedTaskCounts.add(9999);

        strat.selectWorker(queuedTaskCounts);
        strat.selectWorker(queuedTaskCounts);
        strat.selectWorker(queuedTaskCounts);
        int actual = strat.selectWorker(queuedTaskCounts);
        int expected = 0;

        assertEquals("Expected " + expected + " but was " + actual, expected, actual);
    }
}
