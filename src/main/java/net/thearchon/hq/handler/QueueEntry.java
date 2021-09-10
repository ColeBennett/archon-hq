package net.thearchon.hq.handler;

import net.thearchon.hq.Player;

public class QueueEntry {

    private final Player player;
    private final long created;
    private int position;
    private long estimatedWaitTime;

    public QueueEntry(Player player) {
        this.player = player;
        created = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public long getCreated() {
        return created;
    }

    public int getPosition() {
        return position;
    }

    public long getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setEstimatedWaitTime(long estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }
}
