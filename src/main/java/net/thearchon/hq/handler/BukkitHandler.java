package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerRegion;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.nio.Protocol;

import java.util.*;

public abstract class BukkitHandler<T extends BukkitClient> extends PriorityHandler<T> {

    public BukkitHandler(Archon archon) {
        super(archon);

        addHandler((ConnectHandler<T>) client -> {
            client.setUptime(System.currentTimeMillis());
            getActiveClients().add(client);

            archon.getTemplateManager().update(client);

            client.send(Protocol.CONFIG.buffer()
                    .writeString("blockedWords")
                    .writeAll(archon.getSettings().getBlockedWords().values()));
        });
        addHandler((DisconnectHandler<T>) client -> {
            client.reset();
            getActiveClients().remove(client);
        });
    }

    public T getClient(String serverName) {
        for (T client : getClients()) {
            if (client.getServerName().equalsIgnoreCase(serverName)) {
                return client;
            }
        }
        return null;
    }

    public int getOnlineCount() {
        int total = 0;
        for (T client : getActiveClients()) {
            total += client.getOnlineCount();
        }
        return total;
    }

    public int getSlots() {
        int total = 0;
        for (T client : getActiveClients()) {
            total += client.getSlots();
        }
        return total;
    }

    public T availableServer(Player player) {
        return availableServer(player.getRegion());
    }

    public T availableServer(ServerRegion region) {
        if (getOnlineCount() < 100) {
            return fillOrdered(region);
        }
        Map<T, Integer> counts = new HashMap<>(getActiveClients().size());
        for (T client : getActiveClients()) {
            if (client.isLocked()) continue;
            if (!client.wasUpdatedInLast(30000)) continue;
            if (client.getRegion() == region && client.isActive()) {
                counts.put(client, client.getOnlineCount());
            }
        }
        List<T> keys = sortedValueKeys(counts);
        return !keys.isEmpty() ? keys.get(0) : null;
    }

    private T fillOrdered(ServerRegion region) {
        T found = null;
        for (T client : getClients()) {
            if (client.isLocked()) continue;
            if (!client.wasUpdatedInLast(30000)) continue;
            if (client.getRegion() == region && client.getSlots() != 0 && getActiveClients().contains(client)) {
                if (((double) client.getOnlineCount() / (double) client.getSlots()) <= .65) {
                    found = client;
                    break;
                }
            }
        }
        if (found == null) {
            for (T client : getClients()) {
                if (client.isLocked()) continue;
                if (!client.wasUpdatedInLast(30000)) continue;
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

    private static <C> List<C> sortedValueKeys(Map<C, Integer> map) {
        List<C> keys = new LinkedList<>(map.keySet());
        Collections.sort(keys, (o1, o2) -> map.get(o1).compareTo(map.get(o2)));
        return keys;
    }

//    private boolean checkActive(T client) {
//        ChannelId id = client.getChannel().getId();
//        server.getLogger().info("Client disconnected: " + id);
//        if (server.getActiveClients().remove(id) != null) {
//            Handler<Client> handler = server.getHandler(client.getType());
//            boolean registered = false;
//            for (Client c : handler.getActiveClients()) {
//                ChannelId chId = c.getChannel().getId();
//                if (chId != null && chId.equals(id)) {
//                    registered = true;
//                    break;
//                }
//            }
//            if (registered) {
//                try {
//                    handler.clientDisconnected(client);
//                    server.getLogger().info("Unregistered " + client.getType().name() + " client: " + id);
//                } catch (Throwable t) {
//                    server.getLogger().log(Level.SEVERE, "Failed to execute " + handler.getClass().getSimpleName() + ".clientDisconnected()", t);
//                }
//            }
//            client.setChannel(null);
//            return true;
//        } else {
//            server.getLogger().warning("Failed to handle client disconnect: " + id);
//            return false;
//        }
//    }
}
