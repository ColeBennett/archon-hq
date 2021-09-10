package net.thearchon.hq.app;

public interface Check {

    /**
     * Called at each interval increment.
     * @param outbound outbound buffer to enqueue packets
     */
    void check(OutboundInterval outbound);

    /**
     * @return check interval in seconds
     */
    int getInterval();
}
