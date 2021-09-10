package net.thearchon.hq.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.LobbyClient;
import net.thearchon.hq.handler.factions.FactionsClient;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.JsonPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LobbyHandler extends IdBukkitHandler<LobbyClient> implements Runnable {

    private ScheduledFuture<?> task;
    private boolean restarting;
    private int counter;
    private LobbyClient current;
    private Iterator<LobbyClient> itr;

    public LobbyHandler(Archon archon) {
        super(archon);

        archon.runTaskTimer(new Runnable() {
            final Map<Integer, Integer> index = new HashMap<>();
            @Override
            public void run() {
                Collection<LobbyClient> clients = getClients();
                boolean changed = false;
                for (LobbyClient client : clients) {
                    int id = client.getId();
                    Integer prev = index.get(id);
                    if (prev != null) {
                        if (prev != client.getOnlineCount()) {
                            index.put(id, client.getOnlineCount());
                            changed = true;
                        }
                    } else {
                        index.put(id, client.getOnlineCount());
                        changed = true;
                    }
                }
                if (changed) {
                    BufferedPacket buf = Protocol.LOBBY_COUNT_LIST.buffer(clients.size() * 2);
                    for (LobbyClient client : clients) {
                        buf.writeInt(client.getId());
                        buf.writeInt(client.getOnlineCount());
                    }
                    sendAll(buf);
                }
            }
        }, 1, TimeUnit.SECONDS);

        archon.runTaskTimer(() -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour == 0) {
                if (!restarting) {
                    restarting = true;
                    itr = getClients().iterator();
                    task = archon.runTaskTimer(this, 1, TimeUnit.SECONDS);
                }
            } else if (hour == 1) {
                restarting = false;
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (current == null) {
            if (!itr.hasNext()) {
                task.cancel(true);
                task = null;
                return;
            }
            current = itr.next();
            counter = 60 * 2;
        }
        if (counter >= 60 && (counter % 60) == 0) {
            int minute = counter / 60;
            broadcast(current, "&4This lobby will restart in &c&l" + minute + " &4minute" + (minute == 1 ? "" : "s") + "!");
        } else if (counter > 0 && counter < 60) {
            if (counter == 30 || counter == 15 || counter == 10 || counter <= 5) {
                broadcast(current, "&4This lobby will restart in &c&l" + counter + " &4second" + (counter == 1 ? "" : "s") + "!");
            }
        } else if (counter == 0) {
            current.setLocked(true);
            current.setLockMessage(Message.SERVER_RESTARTING.toString());
            LobbyClient avail = null;
            for (LobbyClient lobby : getActiveClients()) {
                if (lobby.getId() != current.getId()) {
                    avail = lobby;
                    break;
                }
            }
            if (avail != null) {
                for (Player player : current.getPlayers()) {
                    player.error(Message.TO_AVAILABLE_LOBBY);
                    player.connect(avail);
                }
            } else {
                for (Player player : current.getPlayers()) {
                    player.disconnect(Message.NO_AVAILABLE_LOBBIES);
                }
            }
        } else if (counter == -10) {
            server.send(current, Protocol.SHUTDOWN.construct());
        } else if (counter == -15) {
            current.setLocked(false);
            current.setLockMessage(null);
            current = null;
        }
        counter--;
    }

    /**
     * Restart lobbies one at a time.
     */
    public void restartLobbies() {
        if (restarting) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }
        restarting = true;
        itr = getClients().iterator();
        task = server.runTaskTimer(this, 1, TimeUnit.SECONDS);
    }

    private void broadcast(LobbyClient client, String message) {
        message = Message.WARNING_PREFIX + message;
        for (Player player : client.getPlayers()) {
            player.message(message);
        }
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(LobbyClient client, Protocol header, BufferedPacket buf) {

    }

    @Override
    protected void handleConnect(LobbyClient client) {
        int slots = server.getSettings().getLobbySlots();
        client.setSlots(slots);
        client.send(Protocol.NETWORK_SLOT_UPDATE.construct(server.getNetworkSlots()));
        client.send(Protocol.LOBBY_SLOT_UPDATE.construct(slots));
        client.send(server.getServerCountList());

        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        for (Client cl : server.getServerManager().getClients()) {
            if (cl.getType() == ServerType.FACTIONS) {
                FactionsClient fc = (FactionsClient) cl;
                JsonObject o = new JsonObject();
                o.addProperty("server", fc.getServerName());
                if (fc.getYoutuber() != null) {
                    o.addProperty("youtuber", fc.getYoutuber());
                }
                if (fc.getCurrentMap() != null) {
                    o.addProperty("map", fc.getCurrentMap().getKey());
                    o.addProperty("age", fc.getCurrentMap().getValue());
                }
                arr.add(o);
            }
        }
        obj.add("info", arr);
        client.send(new JsonPacket(obj));

        // Lobby list
        BufferedPacket list = Protocol.SERVER_LIST.buffer(getClients().size() * 3);
        for (LobbyClient lobby : getClients()) {
            list.writeInt(lobby.getId());
            list.writeInt(lobby.getOnlineCount());
            list.writeBoolean(lobby.isActive());
        }
        client.send(list);

        sendAll(Protocol.SERVER_STATE_UPDATE.construct(client.getId(), true));
        Message.notifyServerStatus(server, client, true);

        // Server join queues
        for (Client f : server.getClients(ServerType.FACTIONS)) {
            ServerJoinQueue queue = ((FactionsClient) f).getQueue();
            if (queue != null) {
                queue.updateQueueSize(client);
            }
        }
    }

    @Override
    protected void handleDisconnect(LobbyClient client) {
        sendAll(Protocol.SERVER_STATE_UPDATE.construct(client.getId(), false));
        Message.notifyServerStatus(server, client, false);
    }

    public void toAvailableServer(Player player) {
        LobbyClient available = availableServer(player);
        if (available == null) {
            player.error(Message.NO_AVAILABLE_LOBBIES);
            return;
        }
        server.requestConnect(player, available);
    }
}
