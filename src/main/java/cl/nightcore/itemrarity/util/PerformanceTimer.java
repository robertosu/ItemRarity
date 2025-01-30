package cl.nightcore.itemrarity.util;


@SuppressWarnings("unused")
public class PerformanceTimer {
    private long startTime;
    private long endTime;
    private boolean isRunning;

    public void start() {
        startTime = System.nanoTime();
        isRunning = true;
    }

    public void stop() {
        endTime = System.nanoTime();
        isRunning = false;
    }

    public long getDurationNanos() {
        if (isRunning) {
            return System.nanoTime() - startTime;
        }
        return endTime - startTime;
    }

    public double getDurationMillis() {
        return getDurationNanos() / 1_000_000.0;
    }

    public String getFormattedDuration() {
        long nanos = getDurationNanos();
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2f µs", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2f ms", nanos / 1_000_000.0);
        } else {
            return String.format("%.2f s", nanos / 1_000_000_000.0);
        }
    }
}