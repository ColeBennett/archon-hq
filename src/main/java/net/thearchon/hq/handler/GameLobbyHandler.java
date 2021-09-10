package net.thearchon.hq.handler;

import net.thearchon.hq.*;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.language.Message;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.hq.client.GameClient;
import net.thearchon.hq.client.IdBukkitClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class GameLobbyHandler<T extends IdBukkitClient,
        G extends GameClient> extends IdBukkitHandler<T> {

    private final GameHandler<G> gameHandler;

    public GameLobbyHandler(Archon server, GameHandler<G> gameHandler) {
        super(server);

        this.gameHandler = gameHandler;

        addHandler((ConnectHandler<T>) client -> {
            int slots = server.getSettings().getLobbySlots();
            client.setSlots(slots);
            client.send(Protocol.LOBBY_SLOT_UPDATE.construct(slots));
            sendLobbyList(client);
            sendAll(Protocol.SERVER_STATE_UPDATE.construct(client.getId(), true));
            gameHandler.sendServerList(client);
            Message.notifyServerStatus(server, client, true);
        });
        addHandler((DisconnectHandler<T>) client -> {
            sendAll(Protocol.SERVER_STATE_UPDATE.construct(client.getId(), false));
            Message.notifyServerStatus(server, client, false);
        });

        server.runTaskTimer(new Runnable() {
            Map<Integer, Integer> index;
            @Override
            public void run() {
                if (index == null) {
                    index = new HashMap<>(getClients().size());
                    for (T client : getClients()) {
                        index.put(client.getId(), client.getOnlineCount());
                    }
                    return;
                }
                boolean changed = false;
                for (T client : getClients()) {
                    Integer prev = index.get(client.getId());
                    if (prev != null && prev != client.getOnlineCount()) {
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    BufferedPacket buf = Protocol.LOBBY_COUNT_LIST.buffer(getClients().size() * 2);
                    for (T client : getClients()) {
                        buf.writeInt(client.getId());
                        buf.writeInt(client.getOnlineCount());
                        index.put(client.getId(), client.getOnlineCount());
                    }
                    sendAll(buf);
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
        if (header == Protocol.GAME_REQUEST_JOIN) {
            Player player = server.getPlayer(buf.getInt(0));
            if (player == null) {
                return;
            }
            if (buf.hasIndex(1)) {
                G cl = gameHandler.getClient(buf.getInt(1));
                boolean access = false;
                if (cl.getState() == GameState.WAITING) {
                    if (cl.getPlayerCount() < cl.getSlots()) {
                        access = true;
                    }
                } else if (cl.getState() == GameState.STARTING) {
                    if (cl.getPlayerCount() < cl.getSlots()) {
                        access = true;
                    }
                    if (player.hasPermission(Rank.VIP) && cl.getPlayerCount() >= cl.getSlots()) {
                        access = true;
                    }
                } else if (cl.getState() == GameState.IN_PROGRESS) {
                    if (player.hasPermission(Rank.VIP)) {
                        access = true;
                    }
                }
                if (access) {
                    player.connect(cl);
                } else {
                    client.getChannel().respond(request, new BufferedPacket(1).writeBoolean(access));
                }
            } else {
                G cl = gameHandler.findFreeServer();
                client.getChannel().respond(request, cl != null ?
                        new BufferedPacket(1).writeString(cl.getServerName()) : new BufferedPacket(0));
            }
        }
    }

    @Override
    public int getSlots() {
        return super.getSlots() + gameHandler.getSlots();
    }

    @Override
    public int getOnlineCount() {
        return super.getOnlineCount() + gameHandler.getOnlineCount();
    }

    public GameHandler<G> getGameHandler() {
        return gameHandler;
    }

    @Override
    public T availableServer(Player player) {
        ServerRegion region = player.getRegion();
        T found = null;
        for (T client : getClients()) {
            if (client.getRegion() == region && client.getSlots() != 0 && getActiveClients().contains(client)) {
                if (((double) client.getOnlineCount() / (double) client.getSlots()) <= .5) {
                    found = client;
                    break;
                }
            }
        }
        if (found == null) {
            for (T client : getClients()) {
                if (client.getRegion() == region && client.getSlots() != 0 && getActiveClients().contains(client)) {
                    if (client.getOnlineCount() < client.getSlots()) {
                        found = client;
                        break;
                    }
                }
            }
        }
        return found;
    }

    public void toAvailableServer(Player player) {
        T available = availableServer(player);
        if (available == null) {
            player.error(Message.NO_AVAILABLE_LOBBIES);
            return;
        }
        server.requestConnect(player, available);
    }

    private void sendLobbyList(T client) {
        BufferedPacket buf = Protocol.SERVER_LIST.buffer(getClients().size() * 3);
        for (T c : getClients()) {
            buf.writeInt(c.getId());
            buf.writeInt(c.getOnlineCount());
            buf.writeBoolean(c.isActive());
        }
        client.send(buf);
    }
}
