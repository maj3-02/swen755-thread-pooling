package threadpooling.schedule;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LeastLoadedSchedulingStrategyTest {

    private final LeastLoadedSchedulingStrategy strat = new LeastLoadedSchedulingStrategy();

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
    public void testSelectWorkerSorted() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        queuedTaskCounts.add(1); // min, index 0
        queuedTaskCounts.add(2);
        queuedTaskCounts.add(3);
        queuedTaskCounts.add(4);

        int actual = strat.selectWorker(queuedTaskCounts);
        int expected = 0;

        assertEquals("Expected " + expected + " but was " + actual, expected, actual);
    }

    @Test
    public void testSelectWorkerUnsorted() {
        List<Integer> queuedTaskCounts = new ArrayList<>();

        queuedTaskCounts.add(122);
        queuedTaskCounts.add(29);
        queuedTaskCounts.add(39);
        queuedTaskCounts.add(405);
        queuedTaskCounts.add(405);
        queuedTaskCounts.add(28); // min, index 5
        queuedTaskCounts.add(405);

        int actual = strat.selectWorker(queuedTaskCounts);
        int expected = 5;

        assertEquals("Expected " + expected + " but was " + actual, expected, actual);
    }


}
