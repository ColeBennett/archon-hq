package net.thearchon.hq.handler;

import net.thearchon.hq.*;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.LobbyClient;
import net.thearchon.hq.handler.factions.FactionsClient;
import net.thearchon.hq.handler.factions.FactionHandler;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.punish.BanRecord;
import net.thearchon.hq.punish.IpBanRecord;
import net.thearchon.hq.punish.MuteRecord;
import net.thearchon.hq.punish.TempbanRecord;
import net.thearchon.hq.util.Pair;
import net.thearchon.hq.util.DateTimeUtil;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;
import org.bson.Document;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeHandler extends AbstractHandler<BungeeClient> {

    private final Map<Integer, BungeeClient> clients = new LinkedHashMap<>();
    private int prevOnlineCount;

    public BungeeHandler(Archon archon) {
        super(archon);

        /*
         * Update the network online count to all bungee instances every
         * second if the number of players online has changed.
         */
        archon.runTaskTimer(() -> {
            int count = archon.getOnlineCount();
            if (count != prevOnlineCount) {
                prevOnlineCount = count;
                sendAll(Protocol.NETWORK_COUNT_UPDATE.construct(count));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public BungeeClient register(ClientListener channel, BufferedPacket buf) {
        int id = buf.getInt(0);
        BungeeClient client = clients.get(id);
        if (client == null) {
            channel.close();
            server.getLogger().warning("Found unregistered " + getClass().getSimpleName() + ": " + id + ", " + clients.keySet());
            return null;
        }
        return client;
    }

    @Override
    public void requestReceived(Client client, RequestPacket request, Protocol header, BufferedPacket buf) {
        if (header == Protocol.LOGIN_REQUEST) {
            long start = System.currentTimeMillis();

            String uuid = buf.getString(0);

            Player player = server.getPlayerByUuid(uuid);
            if (player != null && player.getCurrentServer() != null) {
//                server.respond(client, request, access(false,
//                        "&cThis account is already logged into the network."));
//                return;
            }

            String addr = buf.getString(1);
            String hostName = buf.getString(2).toLowerCase().trim();
            String virtHostName = buf.getString(3).toLowerCase().trim();

//            if (!virtHostName.matches("[a-z].+")) {
//                if (!(uuid.equals("59dc9f53-b683-4668-a612-a34ea182ff60") || uuid.equals("b3b45011-95de-4f3d-bc72-cd7b1632e532"))) {
//                    server.respond(client, request, access(false,
//                            "&cYou cannot connect using a direct ip address."));
//                    return;
//                }
//            }

            /*
             * Ban Check
             */
            BanRecord record = server.getPunishManager().getRecord(uuid);
            if (record != null) {
                String reason = record.getReason();
                if (record instanceof TempbanRecord) {
                    TempbanRecord tmp = (TempbanRecord) record;
                    if (System.currentTimeMillis() >= tmp.getTime()) {
                        server.getPunishManager().tempbanExpired(uuid);
                        initialConnect(client, request);
                        return;
                    }
                    long time = tmp.getTime() - System.currentTimeMillis();
                    time /= 1000;
                    server.respond(client, request, access(false,
                            "&7You are temporarily banned from &cTheArchon&7:\n\n&7Reason: &f" + (reason.length() > 0 ? reason : "The Ban Hammer has spoken!") + "\n\n&7Expires in: &e" + DateTimeUtil.formatTime(time, false) + "\n&7Appeal at: &f&nwww.TheArchon.net"));
                } else {
                    server.respond(client, request, access(false,
                            "&7You are permanently banned from &cTheArchon&7:\n\n&7Reason: &f" + (reason.length() > 0 ? reason : "The Ban Hammer has spoken!") + "\n\n&7Appeal at: &f&nwww.TheArchon.net"));
                }
                return;
            }

            /*
             * Subscription Check
             */
//            if (!archon.getSubscriptionManager().hasSubscription(uuid)) {
//                archon.respond(client, request, access(false,
//                        "&cYou must have a subscription to join TheArchon.\n\n&7Purchase one @ &6shop.thearchon.net"));
//                return;
//            }

            /*
             * IP Ban Check
             */
            IpBanRecord ipBan = server.getPunishManager().getIpBanRecord(addr);
            if (ipBan != null) {
                String reason = ipBan.getReason();
                server.respond(client, request, access(false,
                        "&7You are permanently banned from &cTheArchon&7:\n\n&7Reason: &f" + (reason != null ? reason : "The Ban Hammer has spoken!") + "\n\n&7Appeal at: &f&nwww.TheArchon.net"));
                return;
            }

            /*
             * VPN/Proxy blocker
             */
            if (server.getHostBlocker().check(uuid, addr, hostName, virtHostName)) {
//                PlayerInfo info = server.getPlayerInfoByUuid(uuid);
//                if (info != null) {
//                    server.getPunishManager().tempban(info, System.currentTimeMillis() + 172800000, "You're not allowed to connect using a VPN or proxy, please switch it off.", "d", 2);
//                }
                server.respond(client, request, access(false, Message.VPN_BLOCKED.toString()));
                return;
            }

            /*
             * Maintenance Mode Check
             */
            if (server.getSettings().getMaintenanceMode()) {
                String v = virtHostName;
                server.getDataSource().getConnection(conn -> {
                    boolean access = false;
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT rank FROM players WHERE uuid = '" + uuid + "'")) {
                        if (rs.next()) {
                            Rank rank = Rank.valueOf(rs.getString("rank"));
                            if (Rank.hasPermission(rank, Rank.HELPER)) {
                                access = true;
                            }
                        }
                    } catch (SQLException ignored) {
                    }
                    if (!access) {
                        server.respond(client, request, access(false,
                                "&cTheArchon currently under maintenance. Check back later!"));
                    } else {
                        initialConnect(client, request);
                    }
                });
                return;
            }

            /*
             * IP Match Check
             */
            if (client.getRemoteAddress() != null) {
                int matches = 0;
                for (Player p : server.getPlayers()) {
                    if (p.getAddress().equalsIgnoreCase(addr)) {
                        matches++;
                    }
                }
                if (matches >= 3) {
                    server.respond(client, request, access(false,
                            "&cOnly a maximum of 3 players with your ip &6&o" + addr + " &care allowed on the network at one time."));
                    return;
                }
            }

            if (virtHostName != null) {
                virtHostName = virtHostName.replace(".thearchon.net", "");
                if (!(virtHostName.equals("pvp") || virtHostName.equals("play"))) {
                    if (virtHostName.equals("uhc")) {
                        server.respond(client, request, access(true, "hosteduhc1"));
                        return;
                    } else {
                        for (FactionsClient f : ((FactionHandler) server.getHandler(ServerType.FACTIONS)).getClients()) {
                            if (f.getServerName().contains(virtHostName)) {
                                server.respond(client, request, access(true, f.getServerName()));
                                return;
                            }
                        }
                    }
                }
            }
            initialConnect(client, request);

            long time = System.currentTimeMillis() - start;
            if (time > 250) {
                server.getLogger().warning("LOGIN_REQUEST TOOK TOO LONG: " + uuid + " (" + time + "ms)");
            }
        }
    }

    private BufferedPacket access(boolean access, String message) {
        return new BufferedPacket(2).writeBoolean(access).writeString(message);
    }

    private void initialConnect(Client client, RequestPacket request) {
        BufferedPacket buf = new BufferedPacket(2);
        LobbyHandler handler = server.getHandler(ServerType.LOBBY);
        LobbyClient available = handler.availableServer(client.getRegion());
        if (available != null) {
            buf.writeBoolean(true);
            buf.writeString(available.getServerName());
        } else {
            buf.writeBoolean(false);
            buf.writeString(Message.NO_AVAILABLE_LOBBIES.error());
        }
        server.respond(client, request, buf);
    }

    @Override
    public void packetReceived(BungeeClient client, Protocol header, BufferedPacket buf) {
        if (header == Protocol.PLAYER_SERVER_SWITCH) {
            Player player = server.getPlayerByUuid(buf.getString(0));
            if (player == null) {
                server.getLogger().severe("Received PLAYER_SERVER_SWITCH but player not online with uuid: " + buf.getString(0));
                return;
            }
            BukkitClient from = player.getCurrentServer();
            if (from != null) {
                from.removePlayer(player);

                // Faction server queues
                if (from instanceof FactionsClient) {
                    checkQueue((FactionsClient) from);
                }
            } else {
                if ((System.currentTimeMillis() - player.getSessionStart()) >= 1000 * 60) {
                    server.getLogger().severe("From server not found [PLAYER_SERVER_SWITCH] proxy: " + client.getId() + ", player: " + player.getName() + ", target: " + buf.getString(1));
                }
            }
            player.setCurrentServer(null);

            BukkitClient to = server.getBukkitClientHolder(buf.getString(1));
            if (to == null) {
                server.getLogger().severe("To server not found [PLAYER_SERVER_SWITCH] proxy: " + client.getId() + ", player: " + player.getName() + ", target: " + buf.getString(1));
                return;
            }
            to.addPlayer(player);
            player.setCurrentServer(to);
            server.getEventManager().firePlayerServerConnect(player, to);

            if (to.getType() == ServerType.LOBBY) {
                // Lobby join message
                List<String> joinMsg = new ArrayList<>();
                joinMsg.add("");
                joinMsg.add("         &c&l&nWelcome to TheArchon");
                joinMsg.add("");
                joinMsg.add("  &6&lFORUMS &bwww.TheArchon.net");
                joinMsg.add("  &2&lSHOP &ashop.thearchon.net");
                joinMsg.add("");
                joinMsg.add("  &3&lNews & Updates &7- " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                if (!server.getSettings().getChangelog().isEmpty()) {
                    for (String change : server.getSettings().getChangelog()) {
                        joinMsg.add("  &7- " + change);
                    }
                }
                player.message(joinMsg);
            }
        } else if (header == Protocol.PLAYER_CONNECT) {
            String uuid = buf.getString(0);
            String name = buf.getString(1);
            String addr = buf.getString(2);
            String host = buf.getString(3).toLowerCase();
            String virtHost = buf.getString(4).toLowerCase();

            Player check = server.getPlayerByUuid(uuid);
            if (check != null) {
                server.getLogger().warning("Player already connected to the network: " + name + "/" + uuid + " - "
                        + (check.getCurrentServer() != null ? check.getCurrentServer().getServerName() : "NONE"));
//                return;
            }

            Player player = null;
            Connection conn = null;
            try {
                conn = server.getDataSource().getConnection();

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, rank, coins, unclaimed_votes FROM players WHERE uuid = '" + uuid + "';")) {
                    if (rs.next()) {
                        player = new Player(rs.getInt("id"), uuid, name, client, addr, virtHost);
                        player.setCoins(rs.getInt("coins"));
                        player.setUnclaimedVotes(rs.getInt("unclaimed_votes"));
                        player.setRank(Rank.valueOf(rs.getString("rank")));
                    } else {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO players (uuid, name, ip_address, seen) VALUES(?, ?, ?, NOW());", Statement.RETURN_GENERATED_KEYS)) {
                            ps.setString(1, uuid);
                            ps.setString(2, name);
                            ps.setString(3, addr);
                            ps.execute();

                            ResultSet keys = ps.getGeneratedKeys();
                            if (!keys.next()) {
                                throw new IllegalStateException("Unable to retrieve auto-generated id: " + name);
                            }
                            int id = keys.getInt(1);
                            keys.close();

                            player = new Player(id, uuid, name, client, addr, virtHost);
                            player.setRank(Rank.DEFAULT);

                            server.getCache().setCurrentNewPlayers(server.getCache().getCurrentNewPlayers() + 1);

                            int joined = 0;
                            try (ResultSet count = stmt.executeQuery("SELECT COUNT(*) AS joined FROM players;")) {
                                if (count.next()) {
                                    joined = count.getInt("joined");
                                }
                            }
                            server.sendAll(Protocol.BROADCAST.construct("&6Welcome &7" + name + " &6to &c&lTheArchon &8< &a#" + Util.addCommas(joined) + " &8<"), ServerType.LOBBY);
                        } catch (SQLException e) {
                            server.getLogger().log(Level.SEVERE, "Failed to create new player record: " + name, e);
                        }
                    }
                    stmt.executeUpdate(String.format("INSERT INTO ip_log VALUES(%d, '%s', NOW()) ON DUPLICATE KEY UPDATE date = NOW();", player.getId(), addr));
                } catch (SQLException e) {
                    server.getLogger().log(Level.SEVERE, "Failed to cache player record: " + name, e);
                }
            } catch (Exception e) {
                server.getLogger().log(Level.SEVERE, "Failed to create mysql connection: " + name, e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    server.getLogger().log(Level.WARNING, "Failed to close mysql connection", e);
                }
            }

            if (player != null) {
                onPlayerCreated(player, client, buf);
            } else {
                server.getLogger().warning("Failed to create player: " + name);
            }
        } else if (header == Protocol.PLAYER_DISCONNECT) {
            String uuid = buf.getString(0);
            Player player = server.getPlayerByUuid(uuid);
            if (player != null) {
                client.removePlayer(player);
                onPlayerDisconnectFromNetwork(player);
            } else {
                server.getLogger().severe("Received PLAYER_DISCONNECT but player is not online with uuid: " + buf.getString(0));
            }

            for (BukkitClient cl : server.getBukkitClientHolders()) {
                Iterator<Player> itr = cl.getPlayers().iterator();
                while (itr.hasNext()) {
                    Player p = itr.next();
                    if (p.getUuid().equals(uuid)) {
                        server.getLogger().warning(uuid + " (POST-DISCONNECT) disconnected and was on another server (" + cl.getServerName() + ")");
                        itr.remove();
                    }
                }
            }
        } else if (header == Protocol.PLAYER_CHAT) {
            Player player = server.getPlayerByUuid(buf.getString(0));
            if (player != null) {
                String message = buf.getString(1);
                if (player.isStaff() && player.hasStaffChatEnabled()) {
                    if (player.hasStaffSilent()) {
                        player.error("You cannot talk in staff chat with staff silent mode enabled. Use /sc silent to disable.");
                        return;
                    }
                    server.onStaffChatMessage(player.getName(), player.getRank(), message);
                    return;
                }
                Message.checkMessage(server, player, message);
                server.getEventManager().firePlayerChat(player, message);

                /*
                 * Mute Check
                 */
                if (server.getPunishManager().hasMuteRecord(player.getId())) {
                    MuteRecord record = server.getPunishManager().getMuteRecord(player.getId());
                    String reason = record.getReason();
                    Long endTime = record.getTime();
                    if (endTime != null && endTime < System.currentTimeMillis()) {
                        server.getPunishManager().removeExpiredMuteRecord(player.getId());
                        return;
                    }
                    player.message("&c&lYou are currently muted!"
                            + "\n&7Reason: &f" + (reason != null && reason.length() > 1 ? reason : "None")
                            + "\n&7Expires in: &e" + (endTime != null ? DateTimeUtil.formatTime((endTime - System.currentTimeMillis()) / 1000, false) : "Never")
                            + "\n&7Appeal at: &cwww.TheArchon.net");
                }
            }
        } else if (header == Protocol.PLAYER_COMMAND) {
            Player player = server.getPlayerByUuid(buf.getString(0));
            if (player != null) {
                String command = buf.getString(1);
                String[] args;
                if (buf.hasIndex(2)) {
                    args = new String[buf.size() - 2];
                    for (int i = 2; i < buf.size(); i++) {
                        args[i - 2] = buf.getString(i);
                    }
                } else {
                    args = new String[0];
                }
                StringBuilder cmdBuf = new StringBuilder(command);
                for (String arg : args) {
                    cmdBuf.append(arg);
                }
                Message.checkMessage(server, player, cmdBuf.toString());
                server.getEventManager().firePlayerCommand(player, command, args);
            }
        } else if (header == Protocol.EXEC_COMMAND) {
            Player player = server.getPlayerByUuid(buf.getString(0));
            if (player != null) {
                String command = buf.getString(1);
                String[] args;
                if (buf.hasIndex(2)) {
                    args = new String[buf.size() - 2];
                    for (int i = 2; i < buf.size(); i++) {
                        args[i - 2] = buf.getString(i);
                    }
                } else {
                    args = new String[0];
                }
                server.getCommandManager().runCommand(command, player, args);
                server.getEventManager().firePlayerCommand(player, command, args);
            }
        }
    }

    @Override
    public void clientConnected(BungeeClient client) {
        client.setUptime(System.currentTimeMillis());

        // Status
        Settings config = server.getSettings();
        client.send(Protocol.NETWORK_COUNT_UPDATE.construct(server.getOnlineCount()));
        client.send(Protocol.NETWORK_SLOT_UPDATE.construct(server.getNetworkSlots()));
        client.send(Protocol.MOTD_UPDATE.construct(config.getMaintenanceMode() ? config.getMaintenanceMotd() : config.getMotd(client.getRegion())));

        // Server list
        sendServerList(client);

        // Global commands
        Set<String> commandNames = server.getCommandManager().getCommandNames();
        BufferedPacket commands = Protocol.REGISTER_PROXY_COMMANDS.buffer(commandNames.size());
        for (String command : commandNames) {
            commands.writeString(command);
        }
        client.send(commands);

        for (Player player : server.getPlayers()) {
            if (player.isStaff() && player.hasStaffChatEnabled()) {
                client.send(Protocol.DISABLE_CHAT.construct(player.getName()));
            }
        }

        getActiveClients().add(client);
    }

    @Override
    public void clientDisconnected(BungeeClient client) {
        client.reset();

        Iterator<Player> itr = client.getPlayers().iterator();
        while (itr.hasNext()) {
            Player player = itr.next();
            onPlayerDisconnectFromNetwork(player);
            itr.remove();
            server.getLogger().info("[bungee" + client.getId() + "] Removed player due to proxy disconnect");
        }
        getActiveClients().remove(client);
    }

    @Override
    public void addClient(BungeeClient client) {
        clients.put(client.getId(), client);
    }

    @Override
    public void removeClient(BungeeClient client) {
        clients.remove(client.getId());
    }

    @Override
    public Collection<BungeeClient> getClients() {
        return clients.values();
    }

    public BungeeClient getClient(int id) {
        return clients.get(id);
    }

    private final DecimalFormat format = new DecimalFormat("#");

    private void handlePlayerConnect(Player player) {
        // Check pending purchases that need to be accepted
        server.runTaskLater(() -> server.getPaymentLog().checkPendingAccepts(player), 1500, TimeUnit.MILLISECONDS);

        if (player.hasPermission(Rank.ADMIN)) {
            server.runTaskLater(() -> server.getDataSource().getCollection("archon", "dailytop").count((result, t) -> {
                if (result == null) {
                    result = 0L;
                }

                List<String> msg = new ArrayList<>();
                msg.add("&8" + Message.BAR);
                msg.add("&6[&c&lArchon&6] &aProxy " + player.getProxy().getId() + " &7[" + player.getRegion().name() + " Network]");
                msg.add("");
                msg.add("&e[Today]");
                int mostOnlineToday = server.getCache().getCurrentMostOnline();
                int uniqueLoginsToday = (int) (long) result;
                int newPlayersToday = server.getCache().getCurrentNewPlayers();

                try (Connection conn = server.getDataSource().getConnection();
                     Statement stmt = conn.createStatement()) {
                    // 1-day stats
                    try (ResultSet rs = stmt.executeQuery("SELECT most_online, unique_logins, new_players FROM dailytop WHERE date = (SELECT MAX(date) FROM dailytop)")) {
                        if (rs.next()) {
                            int mostOnlineYesterday = rs.getInt("most_online");
                            int uniqueLoginsYesterday = rs.getInt("unique_logins");
                            int newPlayersYesterday = rs.getInt("new_players");

                            double mostOnlineChange = Util.getPercentageChange(mostOnlineYesterday, mostOnlineToday);
                            String disp = (mostOnlineChange < 0 ? "&7(&c" : "&7(&a+") + format.format(mostOnlineChange) + "% &7from yesterday)";
                            msg.add("&7* &6Most Online: &d" + Util.addCommas(mostOnlineToday) + " " + disp);

                            double uniqueLoginsChange = Util.getPercentageChange(uniqueLoginsYesterday, uniqueLoginsToday);
                            disp = (uniqueLoginsChange < 0 ? "&7(&c" : "&7(&a+") + format.format(uniqueLoginsChange) + "% &7from yesterday)";
                            msg.add("&7* &6Unique Logins: &d" + Util.addCommas(uniqueLoginsToday) + " " + disp);

                            double newPlayersChange = Util.getPercentageChange(newPlayersYesterday, newPlayersToday);
                            disp = (newPlayersChange < 0 ? "&7(&c" : "&7(&a+") + format.format(newPlayersChange) + "% &7from yesterday)";
                            msg.add("&7* &6New Players: &d" + Util.addCommas(newPlayersToday) + " " + disp);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    ZonedDateTime time = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
                    msg.add("&e[From yesterday at this hour: " + time.format(DateTimeFormatter.ofPattern("h a")) + " " + time.getZone().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "]");
                    // prev day (same hour) stats
                    try (ResultSet rs = stmt.executeQuery("SELECT online, most_online, unique_logins, new_players FROM daily_stats WHERE DATE(time) = DATE(SUBDATE(UTC_TIMESTAMP, 1)) && HOUR(time) = HOUR(UTC_TIMESTAMP) LIMIT 1;")) {
                        if (rs.next()) {
                            int onlineYesterday = rs.getInt("online");
                            int mostOnlineYesterday = rs.getInt("most_online");
                            int uniqueLoginsYesterday = rs.getInt("unique_logins");
                            int newPlayersYesterday = rs.getInt("new_players");

                            double currentOnlineChange = Util.getPercentageChange(onlineYesterday, server.getOnlineCount());
                            String disp = (currentOnlineChange < 0 ? "&7(&c" : "&7(&a+") + format.format(currentOnlineChange) + "% &7from yesterday at this hour)";
                            msg.add("&7* &3Online: &b" + Util.addCommas(onlineYesterday) + " " + disp);

                            double mostOnlineChange = Util.getPercentageChange(mostOnlineYesterday, mostOnlineToday);
                            disp = (mostOnlineChange < 0 ? "&7(&c" : "&7(&a+") + format.format(mostOnlineChange) + "% &7from yesterday at this hour)";
                            msg.add("&7* &3Most Online: &b" + Util.addCommas(mostOnlineYesterday) + " " + disp);

                            double uniqueLoginsChange = Util.getPercentageChange(uniqueLoginsYesterday, uniqueLoginsToday);
                            disp = (uniqueLoginsChange < 0 ? "&7(&c" : "&7(&a+") + format.format(uniqueLoginsChange) + "% &7from yesterday at this hour)";
                            msg.add("&7* &3Unique Logins: &b" + Util.addCommas(uniqueLoginsYesterday) + " " + disp);

                            double newPlayersChange = Util.getPercentageChange(newPlayersYesterday, newPlayersToday);
                            disp = (newPlayersChange < 0 ? "&7(&c" : "&7(&a+") + format.format(newPlayersChange) + "% &7from yesterday at this hour)";
                            msg.add("&7* &3New Players: &b" + Util.addCommas(newPlayersYesterday) + " " + disp);
                        } else {
                            msg.add("&7&oNo data to display from this time.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    msg.add("&8" + Message.BAR);

                    try (ResultSet rs = stmt.executeQuery("SELECT (SELECT COUNT(*) FROM votes) AS total, COUNT(*) AS today FROM votes WHERE DATE(time) = CURDATE();")) {
                        if (rs.next()) {
                            msg.add("&aVotes Today: &7" + Util.addCommas(rs.getInt("today"))
                                    + "&a, Total Votes: &7" + Util.addCommas(rs.getInt("total")) + " (" + Util.humanReadableNumber(rs.getInt("total")) + ")");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                player.message(msg);
            }), 1, TimeUnit.SECONDS);
        }
    }

    private void checkQueue(FactionsClient client) {
        ServerJoinQueue queue = client.getQueue();
        if (queue != null && client.getOnlineCount() < client.getSlots()) {
            queue.poll();
        }
    }

    private void onPlayerDisconnectFromNetwork(Player player) {
        if (server.removePlayer(player)) {
            BukkitClient currentServer = player.getCurrentServer();
            String serverName = null;
            if (currentServer != null) {
                serverName = currentServer.getServerName();
                currentServer.removePlayer(player);
                player.setCurrentServer(null);

                // Faction server queues
                if (currentServer instanceof FactionsClient) {
                    checkQueue((FactionsClient) currentServer);
                }
            } else {
                server.getLogger().warning(player + " disconnected without having an assigned server - Online For: "
                        + DateTimeUtil.formatTime((System.currentTimeMillis() - player.getSessionStart()) / 1000, true));
            }

            for (BukkitClient cl : server.getBukkitClientHolders()) {
                if (cl.hasPlayer(player)) {
                    server.getLogger().warning(player + " disconnected and was on another server (" + cl.getServerName() + ") supposed to be on "
                            + (serverName != null ? serverName : "NULL"));
                    cl.removePlayer(player);
                    continue;
                }
                Iterator<Player> itr = cl.getPlayers().iterator();
                while (itr.hasNext()) {
                    Player p = itr.next();
                    if (p.getUuid().equals(player.getUuid())) {
                        server.getLogger().warning(player + " (UUID MATCH) disconnected and was on another server (" + cl.getServerName() + ") supposed to be on "
                                + (serverName != null ? serverName : "NULL"));
                        itr.remove();
                        continue;
                    }
                    if (p.getName().equalsIgnoreCase(player.getName())) {
                        server.getLogger().warning(player + " (NAME MATCH) disconnected and was on another server (" + cl.getServerName() + ") supposed to be on "
                                + (serverName != null ? serverName : "NULL"));
                        itr.remove();
                    }
                }
            }

//            Party party = player.getParty();
//            if (party != null) {
//                party.message(player.getDisplayName() + " &cleft the network. The party has ended!");
//                player.setParty(null);
//                for (Player member : party.getMembers()) {
//                    member.setParty(null);
//                }
//            }
            server.getDataSource().execute("UPDATE players SET seen = NOW() WHERE id = ?;", player.getId());

            server.getEventManager().firePlayerDisconnected(player);
        } else {
            server.getLogger().severe("(Failed disconnect) Player already removed: " + player);
        }
    }

    private void onPlayerCreated(Player player, BungeeClient client, BufferedPacket buf) {
        if (buf.hasIndex(5)) {
            String serv = buf.getString(5);
            BukkitClient cl = server.getBukkitClientHolder(serv);
            if (cl != null) {
                cl.addPlayer(player);
                player.setCurrentServer(cl);
            } else {
                player.disconnect("&cLost connection from the network:\n\n&7&oAuthentication error");
                server.getLogger().warning("Server not found for initial player login: " + serv);
                return;
            }
        }

        client.addPlayer(player);
        server.addPlayer(player);
        handlePlayerConnect(player);

        server.getAppHandler().sendAll(Protocol.PLAYER_CONNECT.construct(
                player.getId(),
                player.getName(),
                player.getRank().name(),
                player.getProxy().getId(),
                (player.getCurrentServer() != null ? player.getCurrentServer().getServerName() : "none")));

        Pair<Client, RequestPacket> failed = server.getFailedLogins().remove(player.getName());
        if (failed != null) {
            server.getLogger().warning("Responded to failed login info for player: " + player.getName());
            server.respond(failed.getX(), failed.getY(), new BufferedPacket(3)
                    .writeInt(player.getId())
                    .writeString(player.getRank().name())
                    .writeInt(player.getCoins()));
        }

        int onlineCount = server.getOnlineCount();
        if (onlineCount > server.getCache().getCurrentMostOnline()) {
            server.getCache().setCurrentMostOnline(onlineCount);
        }
        if (onlineCount > server.getCache().getRecord()) {
            server.getCache().setRecord(onlineCount);
            server.alert(Message.RECORD.format(Util.addCommas(onlineCount)));
        }

        server.getDataSource().execute("UPDATE players SET name = ?, ip_address = ?, seen = NOW() WHERE id = ?;", player.getName(), player.getAddress(), player.getId());
        server.getDataSource().getCollection("archon", "dailytop")
                .insertOne(new Document("_id", player.getId()), (result, t) -> {});

        if (server.getPunishManager().hasMuteRecord(player.getId())) {
            client.send(Protocol.DISABLE_CHAT.construct(player.getUuid()));
        }

        server.getEventManager().firePlayerConnected(player);
    }

    public void updateServerLists() {
        for (BungeeClient client : getActiveClients()) {
            sendServerList(client);
        }
    }

    public void sendServerList(BungeeClient client) {
        Set<BukkitClient> bukkitClients = server.getBukkitClientHolders();
        BufferedPacket serverList = Protocol.SERVER_LIST.buffer(bukkitClients.size() * 3);
        for (BukkitClient bk : bukkitClients) {
            serverList.writeString(bk.getServerName());
            serverList.writeString(bk.getIpAddress());
            serverList.writeInt(bk.getPort());
        }
        client.send(serverList);
    }
}
