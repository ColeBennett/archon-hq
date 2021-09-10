package net.thearchon.hq.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public final class DateTimeUtil {

    public static final DateFormat YYYY_MM_DD_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat HOUR_AM_PM_FORMAT = new SimpleDateFormat("hh:mm a");

    public static final int SECONDS_IN_DAY = (int) TimeUnit.DAYS.toSeconds(1);

    public static String formatTimeMillis(long millis) {
        return formatTime(millis / 1000);
    }

    public static String formatTime(long seconds) {
        return formatTime(seconds, true);
    }

    public static String formatTime(long seconds, boolean shortenNames) {
        int days = (int) seconds / (60 * 60 * 24);
        seconds -= days * (60 * 60 * 24);

        int hours = (int) seconds / (60 * 60);
        seconds -= hours * (60 * 60);

        int minutes = (int) seconds / 60;
        seconds -= minutes * 60;

        StringBuilder buf = new StringBuilder();
        if (days > 0) {
            buf.append(days).append(shortenNames ? "d" : days != 1 ? " days" : " day");
        }
        if (hours > 0) {
            if (days > 0) {
                buf.append(' ');
            }
            buf.append(hours).append(shortenNames ? "h" : hours != 1 ? " hours" : " hour");
        }
        if (minutes > 0) {
            if (days > 0 || hours > 0) {
                buf.append(' ');
            }
            buf.append(minutes).append(shortenNames ? "m" : minutes != 1 ? " minutes" : " minute");
        }
        if (seconds > 0) {
            if (days > 0 || hours > 0 || minutes > 0) {
                buf.append(' ');
            }
            buf.append(seconds).append(shortenNames ? "s" : seconds != 1 ? " seconds" : " second");
        }
        return buf.toString();
    }

    private DateTimeUtil() {}
}
