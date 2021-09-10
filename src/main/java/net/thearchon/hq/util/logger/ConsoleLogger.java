package net.thearchon.hq.util.logger;

import jline.console.ConsoleReader;
import net.thearchon.hq.Archon;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ConsoleLogger extends Logger {

    public ConsoleLogger(Archon server, ConsoleReader consoleReader) {
        super("Logger", null);

        LogFormatter formatter = new LogFormatter(server);

        ConsoleWriter handler = new ConsoleWriter(consoleReader);
        handler.setFormatter(new LogFormatter(server));
        addHandler(handler);

        setLevel(Level.ALL);

        new File("logs").mkdir();

        System.setOut(new PrintStream(new LoggingOutputStream(this, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(this, Level.SEVERE), true));

        try {
            FileHandler errLog = new FileHandler("logs/error.log");
            errLog.setFormatter(new Formatter() {
                final DateFormat format = new SimpleDateFormat("[MMM dd, hh:mm a] ");
                @Override
                public String format(LogRecord record) {
                    return format.format(new Date()) + formatter.format(record);
                }
            });
            errLog.setFilter(record -> record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE);
            addHandler(errLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
