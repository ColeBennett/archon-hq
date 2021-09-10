package net.thearchon.hq.app.checks;

import net.thearchon.hq.Archon;
import net.thearchon.hq.app.AbstractCheck;
import net.thearchon.hq.app.OutboundInterval;

public class ProxyServerCheck extends AbstractCheck {

    private final Archon archon;

    public ProxyServerCheck(Archon archon, int interval) {
        super(interval);

        this.archon = archon;
    }

    @Override
    public void check(OutboundInterval outbound) {
//        BungeeHandler handler = archon.getHandler(ServerType.BUNGEE);
//        Collection<BungeeClient> clients = handler.getClients();
//        if (!clients.isEmpty()) {
//            long curr = System.currentTimeMillis();
//            for (BungeeClient proxy : clients) {
//                if (proxy.isActive() && (curr - proxy.getLastUpdated()) <= 20000) {
//                    ProxyInfo info = new ProxyInfo(proxy.getId(), proxy.getIpAddress());
//                    info.setOnlineCount(proxy.getOnlineCount());
//                    info.setUptime(proxy.getUptime());
//                    info.setFreeMemory(proxy.getFreeMemory());
//                    info.setMaxMemory(proxy.getMaxMemory());
//                    info.setTotalMemory(proxy.getTotalMemory());
//                    outbound.queue(new PacketProxyInfoUpdate(info));
//                }
//            }
//        }
    }
}
