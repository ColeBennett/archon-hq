package net.thearchon.hq.task.tasks;

import net.thearchon.hq.Archon;

public class ExpiredPunishmentsTask implements Runnable {

    private final Archon archon;

    public ExpiredPunishmentsTask(Archon archon) {
        this.archon = archon;
    }

    @Override
    public void run() {
        archon.getPunishManager().removeExpiredTempbans();
        archon.getPunishManager().removeExpiredMutes();
    }
}
