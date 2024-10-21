package Fianco.AI.Search.MoveOrdering;

import java.util.concurrent.atomic.AtomicLong;

public class MoveStats {
    private final AtomicLong sum = new AtomicLong();
    private final AtomicLong count = new AtomicLong();

    public void update(long value) {
        sum.addAndGet(value);
        count.incrementAndGet();
    }

    public double getMean() {
        long currentSum;
        long currentCount;

        // Loop to retry if values change during read
        while (true) {
            long sumBefore = sum.get();
            long countBefore = count.get();

            // Read the values again to check if they have changed
            long sumAfter = sum.get();
            long countAfter = count.get();

            if (sumBefore == sumAfter && countBefore == countAfter) {
                currentSum = sumBefore;
                currentCount = countBefore;
                break;
            }
            // Values changed during read; retry
        }

        return currentCount == 0 ? 0 : (double) currentSum / currentCount;
    }
}