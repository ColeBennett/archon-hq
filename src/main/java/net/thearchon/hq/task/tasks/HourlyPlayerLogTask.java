package net.thearchon.hq.task.tasks;

import net.thearchon.hq.Archon;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HourlyPlayerLogTask implements Runnable {

    private final Archon archon;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
    private int lastHour = ZonedDateTime.now(ZoneOffset.UTC).getHour();

    public HourlyPlayerLogTask(Archon archon) {
        this.archon = archon;
    }

    @Override
    public void run() {
        ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC);
        int hour = time.getHour();
        if (hour != lastHour) {
            archon.getDataSource().getCollection("archon", "dailytop").count((result, t) -> {
                if (result == null) {
                    result = 0L;
                }
                archon.getDataSource().execute("INSERT INTO daily_stats VALUES(?, ?, ?, ?, ?);",
                        time.minusHours(1).format(format),
                        archon.getOnlineCount(),
                        archon.getCache().getCurrentNewPlayers(),
                        archon.getCache().getCurrentMostOnline(),
                        (int) (long) result);
            });
        }
        lastHour = hour;
    }
}
