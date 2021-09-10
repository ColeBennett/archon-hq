package net.thearchon.hq.task.tasks;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.util.DateTimeUtil;
import net.thearchon.nio.Protocol;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

public class MotdCountdownTask implements Runnable {

    private final Archon archon;

    private boolean enabled;
    private ZonedDateTime start, end;

    public MotdCountdownTask(Archon archon) {
        this.archon = archon;

//        end = ZonedDateTime.of(2017, 8, 12, 11, 0, 0, 0, ZoneId.of("America/Los_Angeles"));
    }

    @Override
    public void run() {
        boolean status = archon.getSettings().getBoolean("motd.countdown.enabled");
        if (status) {
//            try {
//                start = ZonedDateTime.parse(archon.getSettings().getString("motd.countdown.start"));
//                end = ZonedDateTime.parse(archon.getSettings().getString("motd.countdown.end"));
//            } catch (DateTimeParseException e) {
//                archon.getLogger().log(Level.SEVERE, "Failed to cache motd countdown times", e);
//            }

            if (!enabled) {
                try {
                    start = ZonedDateTime.parse(archon.getSettings().getString("motd.countdown.start"));
                    end = ZonedDateTime.parse(archon.getSettings().getString("motd.countdown.end"));
                } catch (DateTimeParseException e) {
                    archon.getLogger().log(Level.SEVERE, "Failed to cache motd countdown times", e);
                }
            }
            enabled = true;
        } else {
            enabled = false;
        }
        if (enabled) {
            archon.sendAll(Protocol.MOTD_UPDATE.construct(
                    archon.getSettings().getMaintenanceMode() ? archon.getSettings().getMaintenanceMotd() :
                            archon.getSettings().getMotd().replace("{countdown}", getTimeDisplay())), ServerType.BUNGEE);
        }
    }

    private String getTimeDisplay() {
        ZonedDateTime now = ZonedDateTime.now(end.getZone());
        if (now.isBefore(start)) {
            // Countdown has not started yet
            return "";
        }

        Duration dur = Duration.between(now, end);
        if (dur.isNegative()) {
            // Countdown ended
            return "";
        }

        long diffSec = dur.toMillis() / 1000;
        long days = diffSec / DateTimeUtil.SECONDS_IN_DAY;
        long secondsDay = diffSec % DateTimeUtil.SECONDS_IN_DAY;
        long seconds = secondsDay % 60;
        long minutes = (secondsDay / 60) % 60;
        long hours = (secondsDay / 3600);

//                if (seconds == 0 && (minutes == 0 || minutes % 30 == 0)) {
//                    if (ending) {
//                        alert("",
//                                Message.PREFIX + "&2&l50% OFF &cCHRISTMAS SALE &4&l&nENDS&r &2&lin &d&l" + Util.formatTime(dur.toMillis() / 1000),
//                                "&ashop.thearchon.net",
//                                "");
//
//                    } else {
//                        alert("",
//                                Message.PREFIX + "&2&l50% OFF &cCHRISTMAS SALE &2&lstarts in &d&l" + Util.formatTime(dur.toMillis() / 1000),
//                                "&ashop.thearchon.net",
//                                "");
//                    }
//                }

        String disp = "";
        if (days > 0) {
            disp += "&c" + days + "d&8/";
        }
        return String.format(disp + "&c%dh&8/&c%dm&8/&c%ds", hours, minutes, seconds);
    }
}
