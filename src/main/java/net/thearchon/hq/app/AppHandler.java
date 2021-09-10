package net.thearchon.hq.app;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.thearchon.hq.Archon;
import net.thearchon.hq.Settings;
import net.thearchon.hq.app.websocket.WebSocketPacket;
import net.thearchon.hq.app.websocket.WebSocketServer;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.AbstractHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.Packet;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AppHandler extends AbstractHandler<AppClient> {

    private final Set<AppClient> clients = new HashSet<>();
    private final PurchaseLog purchaseLog;

    private WebSocketServer wsServer;

    private int uniqueLoginsTotal;
    private int uniqueLoginsToday;
    private int newPlayersToday;

    private String paymentStats;

    public AppHandler(Archon archon) {
        super(archon);

        purchaseLog = new PurchaseLog(this);
        wsServer = new WebSocketServer(archon);
        wsServer.setOnConnect(channel -> {
            Settings conf = archon.getSettings();
            
            List<String> activeUsers = new ArrayList<>();
            for (Channel ch : wsServer.getChannels()) {
                activeUsers.add(ch.attr(WebSocketServer.USERNAME).get());
            }

            wsServer.send(channel, new WebSocketPacket()
                    .set("active_users", activeUsers)

                    .set("online_count", archon.getOnlineCount())
                    .set("slots", archon.getNetworkSlots())
                    .set("unique_logins_total", uniqueLoginsTotal)
                    .set("unique_logins_today", uniqueLoginsToday)
                    .set("most_online_today", archon.getCache().getCurrentMostOnline())
                    .set("new_players_today", newPlayersToday)

                    .set("motd", conf.getMotd())
                    .set("lobby_slots", conf.getLobbySlots())
                    .set("maintenance_motd", conf.getMaintenanceMotd())
                    .set("maintenance_mode", conf.getMaintenanceMode())

//                    .set("server_list", constructServerList())
//                    .set("proxy_list", constructProxyList())
//                    .set("player_list", constructPlayerList())
            );
            if (paymentStats != null) {
                wsServer.sendAll(WebSocketServer.constructJson("payments", paymentStats));
            }
            wsServer.sendAll("user_connect", channel.attr(WebSocketServer.USERNAME).get());
//            wsServer.send(channel, WebSocketServer.constructJson("player_list", constructPlayerList()));
        });
        wsServer.setOnDisconnect(channel -> {
            wsServer.sendAll("user_disconnect", channel.attr(WebSocketServer.USERNAME).get());
        });

        archon.runTaskTimer(() -> {
            wsServer.sendAll(WebSocketServer.constructJson("payments", paymentStats));
        }, 5, 30, TimeUnit.SECONDS);

        archon.runTaskTimer(() -> {
            // Update unique_logins_total
            server.getDataSource().getConnection(conn -> {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM players")) {
                    if (rs.next()) {
                        int value = rs.getInt("count");
                        if (value != uniqueLoginsTotal) {
                            uniqueLoginsTotal = value;
                            wsServer.sendAll("unique_logins_total", value);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            // Update unique_logins_today
            server.getDataSource().getCollection("archon", "dailytop").count((result, t) -> {
                if (result != null) {
                    int value = result.intValue();
                    if (value != uniqueLoginsToday) {
                        uniqueLoginsToday = value;
                        wsServer.sendAll("unique_logins_today", value);
                    }
                }
            });
            // Update new_players_today
            int value = server.getCache().getCurrentNewPlayers();
            if (value != newPlayersToday) {
                newPlayersToday = value;
                wsServer.sendAll("new_players_today", value);
            }
        }, 1, 5, TimeUnit.SECONDS);

        // AHQ Status
        archon.runTaskTimer(() -> {
            Runtime runtime = Runtime.getRuntime();
            wsServer.sendAll("ahq_status", new WebSocketPacket()
                    .set("pps", archon.getPacketsPerSecond())
                    .set("in", archon.getInboundPacketCount())
                    .set("out", archon.getOutboundPacketCount())
                    .set("mem_used", runtime.totalMemory() - runtime.freeMemory())
                    .set("mem_total", runtime.totalMemory()));
        }, 1, 1, TimeUnit.SECONDS);

        /*
         * Interval check to update connected users.
         */
        archon.runTaskTimer(new OutboundInterval(archon, this), 0, 1, TimeUnit.SECONDS);
    }

    public WebSocketServer getWebSocketServer() {
        return wsServer;
    }

    @Override
    public void sendAll(Packet packet) {

    }

    public void sendAllWs(TextWebSocketFrame msg) {
        if (wsServer != null) {
            try {
                wsServer.sendAll(msg);
            } catch (Exception e) {
                server.getLogger().log(Level.SEVERE, "wsServer.sendAll() failed", e);
            }
        }
    }

    @Override
    public AppClient register(ClientListener channel, BufferedPacket buf) {
//        String usernameInput = buf.readString();
//        String passwordInput = buf.readString();
//
//        server.getLogger().warning("checking " + usernameInput + "/" + passwordInput);
//
//        int id = -1;
//        String username = null;
//        String password = null;
//        Rank rank = null;
//
//        try (Connection conn = server.getDataSource().getConnection();
//             Statement stmt = conn.createStatement()) {
//            try (ResultSet rs = stmt.executeQuery("SELECT id, name, rank FROM players WHERE name = '" + usernameInput.toLowerCase() + "'")) {
//                if (rs.next()) {
//                    id = rs.getInt("id");
//                    username = rs.getString("name");
//                    rank = Rank.valueOf(rs.getString("rank"));
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//            if (id != -1) {
//                try (ResultSet rs = stmt.executeQuery("SELECT password FROM archonhq WHERE id = " + id)) {
//                    if (rs.next()) {
//                        password = rs.getString("password");
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        int respCode = 0;
//        boolean success = true;
//        if (password == null) {
//            success = false;
//            respCode = PacketLoginResponse.ERR_NOT_REGISTERED;
//            server.getLogger().warning(username + ": User not registered");
//        }
//        if (password != null && !passwordInput.equalsIgnoreCase(password)) {
//            success = false;
//            respCode = PacketLoginResponse.ERR_INVALID_LOGIN;
//            server.getLogger().warning(username + ": Invalid password [input: " + passwordInput + ", actual: " + password + "]");
//        }
//        if (rank != null && !Rank.hasPermission(rank, Rank.JR_MOD)) {
//            success = false;
//            respCode = PacketLoginResponse.ERR_INSUFFICIENT_PERMISSIONS;
//        }
//        if (success) {
//            channel.send(new PacketLoginResponse(username, rank, "0.1-BETA"));
//            server.getDataSource().execute("UPDATE archonhq SET last_login = NOW(), last_addr = ?, connects = connects + 1 WHERE id = ?", channel.getRemoteAddress().getAddress().getHostAddress(), id);
//        } else {
//            channel.send(new PacketLoginResponse(respCode));
//            return null;
//        }
//
//        // TODO
//        if (wsServer != null) {
//            channel.send(new PacketLoginResponse(PacketLoginResponse.ERR_INVALID_LOGIN));
//            return null;
//        }
        return null;
    }

    @Override
    public void requestReceived(Client client, RequestPacket request, Protocol header, BufferedPacket buf) {

    }

    @Override
    public void packetReceived(AppClient client, Protocol header, BufferedPacket buf) {
        
    }
    
    @Override
    public void clientConnected(AppClient client) {

    }
    
    @Override
    public void clientDisconnected(AppClient client) {

    }

    @Override
    public void addClient(AppClient client) {

    }

    @Override
    public void removeClient(AppClient client) {

    }

    @Override
    public Collection<AppClient> getClients() {
        return clients;
    }

    public Archon getParent() {
        return server;
    }

    public PurchaseLog getPurchaseLog() {
        return purchaseLog;
    }

//    private String constructProxyList() {
//        Collection<BungeeClient> instances = server.getAllClients(ServerType.BUNGEE);
//        Iterator<BungeeClient> itr = instances.iterator();
//        ProxyInfo[] proxies = new PacketProxyList.ProxyInfo[instances.size()];
//        for (int i = 0; i < proxies.length; i++) {
//            BungeeClient proxy = itr.next();
//            PacketProxyList.ProxyInfo info = new PacketProxyList.ProxyInfo(proxy.getId(), proxy.getIpAddress());
//            info.setOnlineCount(proxy.getOnlineCount());
//            info.setUptime(proxy.getUptime());
//            info.setFreeMemory(proxy.getFreeMemory());
//            info.setMaxMemory(proxy.getMaxMemory());
//            info.setTotalMemory(proxy.getTotalMemory());
//            proxies[i] = info;
//        }
//        return JsonUtil.toJsonCompact(proxies);
//    }
//
//    private String constructServerList() {
//        List<ServerInfo> servers = new ArrayList<>();
//        for (Handler<?> h : server.getHandlers().values()) {
//            if (h instanceof BukkitHandler) {
//                BukkitHandler<BukkitClient> bh = (BukkitHandler<BukkitClient>) h;
//                if (bh.getClients() == null) {
//                    continue;
//                }
//                for (BukkitClient c : bh.getClients()) {
//                    ServerInfo info = new ServerInfo(c.getServerName());
//                    info.setAddress(c.getIpAddress());
//                    info.setOnlineCount(c.getOnlineCount());
//                    info.setSlots(c.getSlots());
//                    info.setTps(c.getTps());
//                    info.setUptime(c.getUptime());
//                    info.setFreeMemory(c.getFreeMemory());
//                    info.setMaxMemory(c.getMaxMemory());
//                    info.setTotalMemory(c.getTotalMemory());
//                    servers.add(info);
//                }
//            }
//        }
//        return JsonUtil.toJsonCompact(servers);
//    }

//    private String constructPlayerList() {
//        List<PlayerInfo> list = new ArrayList<>(server.getPlayers().size());
//        for (Player player : server.getPlayers()) {
//            list.add(new PlayerInfo(
//                    player.getId(),
//                    player.getName(),
//                    player.getRank().userRank(),
//                    player.getProxy().getId(),
//                    (player.getCurrentServer() != null ? player.getCurrentServer().getServerName() : "none")));
//        }
//        return JsonUtil.toJsonCompact(list);
//    }
//
//    private String constructUserList() {
//        Set<String> users = new LinkedHashSet<>();
//        for (Channel ch : wsServer.getChannels()) {
//            String username = ch.attr(WebSocketServer.USERNAME).get();
//            if (username != null) {
//                users.add(username);
//            }
//        }
//        return JsonUtil.toJsonCompact(users);
//    }

//    private String constructMonthlyServerPaymentList() {
//        JsonArray list = new JsonArray();
//        try (Connection conn = server.getDataSource().getConnection();
//             Statement stmt = conn.createStatement()) {
//            for (String s : server.getServerManager().getNames()) {
//                if (s.startsWith("faction")) {
//                    ResultSet rs = stmt.executeQuery("SELECT ROUND(SUM(price),2) AS sum, MONTHNAME(date) AS month FROM purchases WHERE " +
//                            "status = 'COMPLETE' " +
//                            "&& server LIKE '%" + s + "%' " +
//                            "&& MONTH(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = MONTH(CURDATE()) " +
//                            "&& YEAR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = 2016");
//                    if (rs.next()) {
//                        JsonObject obj = new JsonObject();
//                        obj.addProperty("server", s);
//                        obj.addProperty("total", rs.getInt("sum"));
//                        obj.addProperty("month", rs.getString("month"));
//                        list.add(obj);
//                    }
//                    rs.close();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return JsonUtil.toJsonCompact(list);
//    }
}
