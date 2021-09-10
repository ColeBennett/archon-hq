package net.thearchon.hq.handler.factions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.LobbyClient;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.handler.NamedBukkitHandler;
import net.thearchon.hq.handler.ServerJoinQueue;
import net.thearchon.hq.language.Message;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FactionHandler extends NamedBukkitHandler<FactionsClient> implements Runnable {

    private ScheduledFuture<?> task;
    private boolean restarting;
    private int counter;
    private FactionsClient current;
    private Iterator<FactionsClient> itr;

    public FactionHandler(Archon archon) {
        super(archon);

        archon.runTaskTimer(() -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour == 0) {
                if (!restarting) {
                    restarting = true;
                    itr = getClients().iterator();
                    task = archon.runTaskTimer(this, 1, TimeUnit.SECONDS);
                }
            } else if (hour == 2) {
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
            counter = 60 * 5;
        }
        if (counter >= 60 && (counter % 60) == 0) {
            int minute = counter / 60;
            broadcast(current, "&4This server will restart in &c&l" + minute + " &4minute" + (minute == 1 ? "" : "s") + '!');
            if (minute == 5) {
                current.runConsoleCommand("aduel disable");
            }
        } else if (counter > 0 && counter < 60) {
            if (counter == 30 || counter == 15 || counter == 10 || counter <= 5) {
                broadcast(current, "&4This server will restart in &c&l" + counter + " &4second" + (counter == 1 ? "" : "s") + '!');
            }
            if (counter == 15) {
                current.runConsoleCommand(
                        "killall mobs world",
                        "killall mobs world_nether",
                        "killall mobs world_the_end");
            }
        } else if (counter == 0) {
            current.setLocked(true);
            current.setLockMessage(Message.SERVER_RESTARTING.toString());

            LobbyHandler h = server.getHandler(ServerType.LOBBY);
            List<LobbyClient> clients = new ArrayList<>(h.getActiveClients());
            int index = 0;
            for (Player player : current.getPlayers()) {
                LobbyClient lobby = clients.get(index);
                if (!lobby.wasUpdatedInLast(30000) || lobby.getOnlineCount() >= lobby.getSlots()) {
                    clients.remove(index);
                    index = 0;
                }
                if (!clients.isEmpty()) {
                    lobby = clients.get(index++);
                    player.message(Message.TO_AVAILABLE_LOBBY);
                    player.connect(lobby);
                    if (index >= clients.size() - 1) {
                        index = 0;
                    }
                } else {
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

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(FactionsClient client, Protocol header, BufferedPacket buf) {
        if (header == Protocol.BROADCAST) {
            sendAll(Protocol.BROADCAST.construct(buf.getString(0)));
        }
    }

    @Override
    protected void handleConnect(FactionsClient client) {

    }

    @Override
    protected void handleDisconnect(FactionsClient client) {

    }

    private void broadcast(FactionsClient client, String message) {
        message = Message.WARNING_PREFIX + message;
        for (Player player : client.getPlayers()) {
            player.message(message);
        }
    }

    public void queueEntry(Player player, FactionsClient client) {
        if (!server.getSettings().getBoolean("queue-enabled")) {
            player.error("The queue will be available soon!");
            return;
        }

        if (client.getOnlineCount() >= client.getSlots()) {
            ServerJoinQueue queue = client.getQueue();
            if (queue != null) {
                queue.queue(player);
            } else {
                player.message(Message.QUEUE_PREFIX + "&cNo queue available for &7" + client.getDisplayName());
            }
        } else {
            player.message(Message.QUEUE_PREFIX + "&dSlot available, connecting you to &7" + client.getDisplayName());
            player.connect(client);
        }
    }
}
