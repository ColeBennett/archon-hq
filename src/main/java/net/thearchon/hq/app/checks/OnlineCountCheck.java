package net.thearchon.hq.app.checks;

import net.thearchon.hq.Archon;
import net.thearchon.hq.app.AbstractCheck;
import net.thearchon.hq.app.OutboundInterval;
import net.thearchon.hq.app.websocket.WebSocketServer;

public class OnlineCountCheck extends AbstractCheck {

    private final Archon archon;
    private int prev;

    public OnlineCountCheck(Archon archon, int interval) {
        super(interval);

        this.archon = archon;
    }

    @Override
    public void check(OutboundInterval outbound) {
        int count = archon.getOnlineCount();
        if (count != prev) {
            prev = count;
            outbound.queue(WebSocketServer.construct("online_count", count));
        }
    }
}
