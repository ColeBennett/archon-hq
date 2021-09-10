package net.thearchon.hq.util.logger;

import net.thearchon.hq.Archon;
import net.thearchon.hq.util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class LogFormatter extends Formatter {
    
    private final Archon server;
    private final Runtime runtime = Runtime.getRuntime();

    LogFormatter(Archon server) {
        this.server = server;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder buf = new StringBuilder();
        
        buf.append("[Thr: ");
        buf.append(Thread.activeCount());

        buf.append(", Mem: ");
        buf.append(Util.addCommas((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024));
        buf.append('/');
        buf.append(Util.addCommas(runtime.totalMemory() / 1024 / 1024));

        buf.append(", Cl: ");
        buf.append(server.getClientCount());
        buf.append(", Packet: ");
        buf.append(Util.humanReadableNumber(server.getTotalPacketCount()));
        buf.append('/');
        buf.append(Util.addCommas(server.getPacketsPerSecond()));
        buf.append("] ");

        if (record.getLevel() != Level.INFO) {
            buf.append('[');
            buf.append(record.getLevel().getName());
            buf.append("] ");
        }

        buf.append(record.getMessage());
        buf.append('\n');
        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            buf.append(writer);
        }
        return buf.toString();
    }
}
