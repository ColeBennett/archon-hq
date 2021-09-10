package net.thearchon.hq.app.checks;

import net.thearchon.hq.Archon;
import net.thearchon.hq.app.AbstractCheck;
import net.thearchon.hq.app.OutboundInterval;
import net.thearchon.hq.client.BukkitClient;

import java.util.Set;

public class BukkitServerCheck extends AbstractCheck {

    private final Archon archon;

    public BukkitServerCheck(Archon archon, int interval) {
        super(interval);

        this.archon = archon;
    }

    @Override
    public void check(OutboundInterval outbound) {
        Set<BukkitClient> clients = archon.getBukkitClientHolders();
        if (!clients.isEmpty()) {
            long curr = System.currentTimeMillis();

//            List<ServerInfo> list = new ArrayList<>();
//            for (BukkitClient server : clients) {
//                if (server.isActive() && (curr - server.getLastUpdated()) <= 20000) {
//                    ServerInfo info = new ServerInfo(server.getServerName());
//                    info.setAddress(server.getIpAddress());
//                    info.setOnlineCount(server.getOnlineCount());
//                    info.setSlots(server.getSlots());
//                    info.setTps(server.getTps());
//                    info.setUptime(server.getUptime());
//                    info.setFreeMemory(server.getFreeMemory());
//                    info.setMaxMemory(server.getMaxMemory());
//                    info.setTotalMemory(server.getTotalMemory());
//                    outbound.queue(new PacketServerInfoUpdate(info));
//                    list.add(info);
//                }
//            }

//            if (!list.isEmpty()) {
//                outbound.queue(WebSocketServer.constructJson("server_list_update", JsonUtil.toJsonCompact(list)));
//            }
        }
    }
}
