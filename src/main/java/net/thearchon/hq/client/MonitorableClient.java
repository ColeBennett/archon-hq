package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.util.Util;
import net.thearchon.hq.ServerType;

public abstract class MonitorableClient extends Client {

    private long uptime, freeMemory, totalMemory, maxMemory;

    /**
     * Last timestamp this client had any of its values changed.
     */
    private long lastUpdated = System.currentTimeMillis();

    public MonitorableClient(Archon archon, ServerType type) {
        super(archon, type);
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean wasUpdatedInLast(long millis) {
        return (System.currentTimeMillis() - lastUpdated) <= millis;
    }

    public long getUptime() {
        return uptime;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getUsedMemory() {
        return totalMemory - freeMemory;
    }

    public double getFreeMemoryMb() {
        return Util.toMb(freeMemory);
    }

    public double getTotalMemoryMb() {
        return Util.toMb(totalMemory);
    }

    public double getMaxMemoryMb() {
        return Util.toMb(maxMemory);
    }

    public double getUsedMemoryMb() {
        return Util.toMb(getUsedMemory());
    }

    public double getFreeMemoryGb() {
        return Util.toGb(freeMemory);
    }

    public double getTotalMemoryGb() {
        return Util.toGb(totalMemory);
    }

    public double getMaxMemoryGb() {
        return Util.toGb(maxMemory);
    }

    public double getUsedMemoryGb() {
        return Util.toGb(getUsedMemory());
    }

    public String getReadableFreeMemory() {
        return Util.humanReadableByteCount(freeMemory, true);
    }

    public String getReadableTotalMemory() {
        return Util.humanReadableByteCount(totalMemory, true);
    }

    public String getReadableMaxMemory() {
        return Util.humanReadableByteCount(maxMemory, true);
    }

    public String getReadableUsedMemory() {
        return Util.humanReadableByteCount(getUsedMemory(), true);
    }

    public int getMemoryUsagePercTotal() {
        double used = getUsedMemory();
        if (used <= 0 || totalMemory <= 0) return 0;
        int perc = (int) ((used / totalMemory) * 100.0);
        return perc > 100 ? 0 : perc;
    }

    public int getMemoryUsagePercMax() {
        double used = getUsedMemory();
        if (used <= 0 || maxMemory <= 0) return 0;
        int perc = (int) ((used / maxMemory) * 100.0);
        return perc > 100 ? 0 : perc;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
        updateTime();
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
        updateTime();
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
        updateTime();
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
        updateTime();
    }

    public void reset() {
        uptime = 0;
        freeMemory = 0;
        totalMemory = 0;
        maxMemory = 0;
        updateTime();
    }

    protected void updateTime() {
        lastUpdated = System.currentTimeMillis();
    }
}
