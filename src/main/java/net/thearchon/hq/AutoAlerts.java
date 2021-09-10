package net.thearchon.hq;

import com.google.gson.reflect.TypeToken;
import net.thearchon.hq.util.io.JsonUtil;
import net.thearchon.nio.Protocol;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoAlerts {

    public static final File FILE = new File("alerts.json");
    private static final Type TYPE = new TypeToken<ArrayList<Alert>>(){}.getType();

    private final Archon archon;
    private List<Alert> alerts;
    private long runId;
    private ScheduledFuture<?> task;

    public AutoAlerts(Archon archon) {
        this.archon = archon;
        load();
    }

    public void load() {
        if (FILE.exists()) {
            alerts = JsonUtil.load(FILE, TYPE);
        } else {
            alerts = new ArrayList<>();
            alerts.add(new Alert(Collections.singletonList("Example Alert"), 10, false));
            JsonUtil.save(FILE, alerts);
        }
        start();
    }

    public void start() {
        stop();
        runId = 1;
        task = archon.runTaskTimer(() -> {
            if (alerts != null) {
                for (Alert alert : alerts) {
                    if (alert.isEnabled() && runId % alert.getDelay() == 0) {
                        archon.sendAll(Protocol.BROADCAST.construct(alert.getMessages()), ServerType.BUNGEE);
                    }
                }
                runId++;
                if (runId == Integer.MAX_VALUE) {
                    runId = 1;
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public static final class Alert {
        private final List<String> messages;
        private final int delay;
        private final boolean enabled;

        public Alert(List<String> messages, int delay, boolean enabled) {
            this.messages = messages;
            this.delay = delay;
            this.enabled = enabled;
        }

        public List<String> getMessages() {
            return messages;
        }

        public int getDelay() {
            return delay;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
