package net.thearchon.hq;

import io.netty.channel.ChannelId;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.IdBukkitClient;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.hq.handler.Handler;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.payment.PaymentLog;
import net.thearchon.hq.punish.report.ReportType;
import net.thearchon.hq.util.Pair;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.Packet;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;
import net.thearchon.nio.server.ServerEventHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NetHandler implements ServerEventHandler {

    private final Archon archon;

    public NetHandler(Archon archon) {
        this.archon = archon;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void packetReceived(ClientListener channel, Packet packet) {
        archon.incInboundPacketCount();

        long start = System.currentTimeMillis();
        if (packet instanceof BufferedPacket) {
            BufferedPacket buf = (BufferedPacket) packet;
            if (!buf.hasShort(0)) return;
            Protocol header = Protocol.valueOf(buf);
            if (header != Protocol.SERVER_MONITOR) {
                archon.getLogger().info("recv [" + header.name().toLowerCase() + "] from " + channel.getId());
            }

            if (header == Protocol.CONNECT) {
                ServerType type;
                try {
                    type = ServerType.valueOf(buf.getString(0));
                } catch (IllegalArgumentException e) {
                    channel.close();
                    archon.getLogger().warning("Client type not found: " + buf.getString(0));
                    return;
                }
                buf.remove(0);
                archon.getLogger().info("Attempting client login: " + type.name() + " - " + buf);

                Handler<Client> handler = archon.getHandlers().get(type);
                if (handler == null) {
                    archon.getLogger().warning("Handler not found: " + type.name());
                    return;
                }

                Client client = handler.register(channel, buf);
                if (client == null) {
                    if (channel.isActive()) {
                        channel.close();
                    }
                    archon.getLogger().warning("Failed to register client: " + channel.getId());
                    return;
                }

                if (client.getType() != ServerType.SETUP) {
                    String ip = channel.getRemoteAddress().getAddress().getHostAddress();
                    if (!client.getIpAddress().equals(ip)) {
                        if (channel.isActive()) {
                            channel.close();
                        }
                        archon.getLogger().warning("Disconnected client. (IP DOESN'T MATCH SET IP FROM servers.json): " + client.getIpAddress() + " != " + ip + " (incoming)");
                        return;
                    }
                }

                client.setChannel(channel);
                archon.getActiveClients().put(channel.getId(), client);

                try {
                    handler.clientConnected(client);
                    archon.getLogger().info("Registered client " + type.name() + ": " + channel.getId());
                } catch (Throwable t) {
                    archon.getLogger().log(Level.SEVERE, "Failed to execute " + handler.getClass().getSimpleName() + ".clientConnected()", t);
                }
                return;
            }

            Client client = archon.getActiveClients().get(channel.getId());
            if (client == null) {
                if (channel.isActive()) {
                    channel.close();
                }
                return;
            }
            client.addPacketReceived();

            if (header == Protocol.EXEC_COMMAND) {
                List<String> args = buf.asList(String.class);
                String label = args.remove(0);
                if (label.equalsIgnoreCase("tempban")) {
                    for (int i = 0; i < args.size(); i++) {
                        String arg = args.get(i);
                        if (arg.equalsIgnoreCase("15m")) {
                            args.set(i, "15");
                            args.add(i + 1, "m");
                            break;
                        }
                    }
                }
                archon.getCommandManager().runConsoleCommand(label, args.toArray(new String[args.size()]));
            } else if (header == Protocol.COIN_UPDATE) {
                int id = buf.getInt(0);
                int coins = buf.getInt(1);
                archon.getDataSource().execute("UPDATE players SET coins = ? WHERE id = ?", coins, id);
                Player player = archon.getPlayer(id);
                if (player != null) {
                    player.setCoins(coins);
                }
//            } else if (header == Protocol.JACKPOT_ADD_TICKET) {
//                archon.getJackpotManager().addTicket(archon.getPlayerByUuid(buf.getString(0)), buf.getString(1), buf.getString(2));
            } else if (header == Protocol.MESSAGE) {
                Player player = null;
                if (buf.hasInt(0)) {
                    player = archon.getPlayer(buf.getInt(0));
                } else if (buf.hasString(0)) {
                    player = archon.getPlayerByUuid(buf.getString(0));
                }
                if (player != null) {
                    player.message(buf.getString(1));
                }
            } else if (header == Protocol.LOG_PURCHASE) {
                String serv = PaymentLog.MINIGAMES_SERVER_NAME;
                if (client instanceof BukkitClient) {
                    BukkitClient bk = (BukkitClient) client;
                    if (!(bk instanceof IdBukkitClient)) {
                        serv = bk.getServerName();
                    }
                }
                String[] parts = new String[buf.size()];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = buf.getString(i);
                }
                archon.getPaymentLog().log(parts, null, serv);
            } else if (header == Protocol.SERVER_MONITOR) {
                if (client instanceof BukkitClient) {
                    BukkitClient bc = (BukkitClient) client;
                    bc.setTps((float) buf.getDouble(0));
                    bc.setFreeMemory(buf.getLong(1));
                    bc.setTotalMemory(buf.getLong(2));
                    bc.setMaxMemory(buf.getLong(3));
                } else if (client instanceof BungeeClient) {
                    BungeeClient bc = (BungeeClient) client;
                    bc.setFreeMemory(buf.getLong(0));
                    bc.setTotalMemory(buf.getLong(1));
                    bc.setMaxMemory(buf.getLong(2));
                }
            } else if (header == Protocol.REQUEST_CONNECT) {
                Player player = archon.getPlayer(buf.getInt(0));
                if (player != null) {
                    String request = buf.getString(1).toLowerCase();
                    if (request.equals("lobby")) {
                        LobbyHandler handler = archon.getHandler(ServerType.LOBBY);
                        handler.toAvailableServer(player);
                        return;
                    }
                    if (request.endsWith("lobby")) {
                        try {
                            ServerType type = ServerType.valueOf(request.replace("lobby", "_LOBBY").toUpperCase());
                            GameLobbyHandler handler = archon.getHandler(type);
                            if (handler == null) {
                                player.error("Server " + request + " does not exist.");
                                return;
                            }
                            handler.toAvailableServer(player);
                        } catch (IllegalArgumentException e) {
                            player.error("Server " + request + " does not exist.");
                        }
                        return;
                    }

                    BukkitClient to = archon.getBukkitClient(request);
                    if (to != null) {
                        archon.requestConnect(player, to);
                    } else {
                        player.error("Server " + request + " is currently offline.");
                    }
                }
            }

            archon.getHandlers().get(client.getType()).packetReceived(client, header, buf);
        } else if (packet instanceof RequestPacket) {
            Client client = archon.getActiveClients().get(channel.getId());
            if (client == null) {
                channel.close();
                archon.getLogger().warning("Found unregistered client: " + channel.getId());
                return;
            }

            RequestPacket request = (RequestPacket) packet;
            if (request.getPacket() instanceof BufferedPacket) {
                BufferedPacket data = (BufferedPacket) request.getPacket();
                Protocol header = Protocol.valueOf(data);
                archon.getLogger().info("recv [" + header.name().toLowerCase() + "] from " + channel.getId());

                if (header == Protocol.GET_PLAYER_INFO) {
                    String value = data.getString(0);
                    Player player = value.contains("-") ? archon.getPlayerByUuid(value) : archon.getPlayerByName(value);
                    if (player == null) {
                        archon.getLogger().severe("Player not found: " + value + ", adding to failed login queue...");
                        archon.getFailedLogins().put(value, new Pair<>(client, request));
                        archon.runTaskLater(() -> archon.getFailedLogins().remove(value), 10, TimeUnit.SECONDS);
                    } else {
                        archon.respond(channel, request, new BufferedPacket(3)
                                .writeInt(player.getId())
                                .writeEnum(player.getRank())
                                .writeInt(player.getCoins()));
                    }
                } else if (header == Protocol.REPORT) {
                    Player player = archon.getPlayer(data.getInt(0));
                    Player target = archon.getPlayer(data.getInt(1));
                    String reason = Util.upperLower(data.getEnum(2, ReportType.class).name());
                    boolean success = false;
                    if (player != null && target != null) {
                        player.message(
                                "&c&l[REPORT] &7Reported " + target.getDisplayName() + " &7for &c" + reason + "&7.",
                                "&cNote: &7Falsely reporting a player could result in a temp-ban from the network.");
                        archon.notifyStaff(player.getDisplayName() + " &8(" + player.getCurrentServer().getServerName() + ") &7reported " + target.getDisplayName() + " &8(" + target.getCurrentServer().getServerName() + ") &7for &c" + reason + "&7.");
                        success = true;
                    }
                    archon.respond(channel, request, new BufferedPacket(1)
                            .writeBoolean(success));
                } else if (header == Protocol.FORWARD) {
                    archon.getHandlers().get(ServerType.valueOf(data.readString())).requestReceived(client, request, Protocol.valueOf(data), data);
                } else {
                    archon.getHandlers().get(client.getType()).requestReceived(client, request, header, data);
                }
            }
        } else {
            Client client = archon.getActiveClients().get(channel.getId());
            if (client == null) {
                channel.close();
                archon.getLogger().warning("Found unregistered client: " + channel.getId());
                return;
            }
            client.addPacketReceived();
        }

        long time = System.currentTimeMillis() - start;
        if (time > 100) {
            archon.getLogger().severe("[INCOMING PACKET] Took too long too handle: " + time + " ms. Data: " + packet);
        }
    }

    @Override
    public void clientConnected(ClientListener channel) {
        ChannelId id = channel.getId();
        archon.getLogger().info("Client connected: " + id);
        archon.runTaskLater(() -> {
            if (archon.getDataServer().hasClient(id) && !archon.getActiveClients().containsKey(id)) {
                channel.close();
                archon.getLogger().warning("Client failed to register within 10 seconds: " + id);
            }
        }, 10, TimeUnit.SECONDS);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void clientDisconnected(ClientListener channel) {
        ChannelId id = channel.getId();
        archon.getLogger().info("Client disconnected: " + id);
        Client client = archon.getActiveClients().remove(id);
        if (client != null) {
            Handler<Client> handler = archon.getHandler(client.getType());
            boolean registered = false;
            for (Client c : handler.getActiveClients()) {
                ChannelId chId = c.getChannel().getId();
                if (chId != null && chId.equals(id)) {
                    registered = true;
                    break;
                }
            }
            if (registered) {
                try {
                    handler.clientDisconnected(client);
                    archon.getLogger().info("Unregistered " + client.getType().name() + " client: " + id);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.SEVERE, "Failed to execute " + handler.getClass().getSimpleName() + ".clientDisconnected()", t);
                }
            }
            client.setChannel(null);
        } else {
            archon.getLogger().warning("Failed to handle client disconnect: " + id);
        }
    }
}
