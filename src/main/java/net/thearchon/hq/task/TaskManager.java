package net.thearchon.hq.task;

import net.thearchon.hq.Archon;
import net.thearchon.hq.task.tasks.DailytopTask;
import net.thearchon.hq.task.tasks.ExpiredPunishmentsTask;
import net.thearchon.hq.task.tasks.HourlyPlayerLogTask;
import net.thearchon.hq.task.tasks.MotdCountdownTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    private final Archon archon;
    private final Map<Runnable, Integer> tasks = new HashMap<>();

    public TaskManager(Archon archon) {
        this.archon = archon;

        tasks.put(new HourlyPlayerLogTask(archon), 1);
        tasks.put(new DailytopTask(archon), 15);
        tasks.put(new MotdCountdownTask(archon), 1);
//        tasks.put(new MonitorReportTask(archon), 30);
        tasks.put(new ExpiredPunishmentsTask(archon), 30);

        for (Entry<Runnable, Integer> task : tasks.entrySet()) {
            archon.runTaskTimer(task.getKey(), 0, task.getValue(), TimeUnit.SECONDS);
        }
    }
}
