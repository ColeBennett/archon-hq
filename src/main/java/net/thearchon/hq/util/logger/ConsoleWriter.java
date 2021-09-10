package net.thearchon.hq.util.logger;

import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

final class ConsoleWriter extends Handler {

    private final ConsoleReader reader;

    ConsoleWriter(ConsoleReader reader) {
        this.reader = reader;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            try {
                reader.print(ConsoleReader.RESET_LINE
                        + getFormatter().format(record)
                        + Ansi.ansi().reset().toString());
                reader.drawLine();
                reader.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
