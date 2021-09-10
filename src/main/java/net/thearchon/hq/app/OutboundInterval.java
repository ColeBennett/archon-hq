package net.thearchon.hq.app;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.thearchon.hq.Archon;
import net.thearchon.hq.app.checks.BukkitServerCheck;
import net.thearchon.hq.app.checks.OnlineCountCheck;
import net.thearchon.hq.app.checks.ProxyServerCheck;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.protocol.Packet;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

public class OutboundInterval implements Runnable {

    private final Archon archon;
    private final AppHandler handler;

    /**
     * Checks to be executed each outbound interval increment.
     */
    private final Set<Check> checks = new LinkedHashSet<>();

    /**
     * Packets to be sent during each interval period.
     */
    private final Queue<Object> batch = new LinkedList<>();

    /**
     * Incremented counter in seconds.
     */
    private long counter;

    OutboundInterval(Archon archon, AppHandler handler) {
        this.archon = archon;
        this.handler = handler;

        checks.add(new OnlineCountCheck(archon, 1));
        checks.add(new BukkitServerCheck(archon, 10));
        checks.add(new ProxyServerCheck(archon, 10));

    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        /**
         * Run through each check at their specified intervals.
         */
        for (Check check : checks) {
            if (counter % check.getInterval() == 0) {
                try {
                    check.check(this);
                } catch (Exception e) {
                    archon.getLogger().log(Level.WARNING, "Failed to run check: " + check.getClass().getSimpleName(), e);
                }
            }
        }
        
        /**
         * Flush queued packets in this interval to all connected users.
         */
        int out = flushBatch();
        if (out > 0) {
            long time = System.currentTimeMillis() - start;
            archon.getLogger().info("App Interval (#" + Util.addCommas(counter) + ") - time: " + time + " ms, packets sent: " + Util.addCommas(out));
        }

        if (counter == Long.MAX_VALUE) {
            counter = 0;
        } else {
            counter++;
        }
    }

    public void queue(Object packet) {
        batch.add(packet);
    }

    private int flushBatch() {
        int sent = 0;
        if (!batch.isEmpty()) {
            Object out;
            while ((out = batch.poll()) != null) {
                if (out instanceof Packet) {
                    handler.sendAll((Packet) out);
                } else if (out instanceof TextWebSocketFrame) {
                    handler.sendAllWs((TextWebSocketFrame) out);
                }
                sent++;
            }
        }
        return sent;
    }
}
