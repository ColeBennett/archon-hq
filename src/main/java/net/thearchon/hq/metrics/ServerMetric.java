package net.thearchon.hq.metrics;

public class ServerMetric {

    private final String serverName;
    private final int[] byMinute = new int[60];
    private final int[] byHour = new int[60];
    private final int[] byDay = new int[7];
    private final int[] byWeek = new int[52];
    private int minuteCounter;

    ServerMetric(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public void logMinute(int onlineCount) {
        byMinute[minuteCounter++] = onlineCount;
        if (minuteCounter >= byMinute.length - 1) {
            minuteCounter = 0;
            reset();
        }
    }

    public int hourLow() {
        int low = byMinute[0];
        for (int i = 1; i < byMinute.length; i++) {
            int v = byMinute[i];
            if (v < low) {
                low = v;
            }
        }
        return low;
    }

    public int hourHigh() {
        int high = 0;
        for (int i : byMinute) {
            if (i > high) {
                high = i;
            }
        }
        return high;
    }

    public int hourAverage() {
        int total = 0;
        for (int i : byMinute) {
            total += i;
        }
        return total / byMinute.length;
    }

    public void reset() {
        for (int i = 0; i < byMinute.length; i++) {
            byMinute[i] = 0;
        }
    }
}
