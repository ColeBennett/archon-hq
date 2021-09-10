package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.server.ClientListener;
import net.thearchon.hq.client.IdBukkitClient;

import java.util.*;

public abstract class IdBukkitHandler<T extends IdBukkitClient> extends BukkitHandler<T> {
    
    private final Map<Integer, T> clients = new TreeMap<>();

    public IdBukkitHandler(Archon archon) {
        super(archon);
    }
    
    @Override
    public final T register(ClientListener channel, BufferedPacket buf) {
        int id = buf.takeInt(0);
        T client = clients.get(id);
        if (client == null) {
            channel.close();
            server.getLogger().warning("Unregistered " + getClass().getSimpleName()
                    + ": " + id + ", " + clients.keySet());
            return null;
        }
        return client;
    }
    
    @Override
    public final void addClient(T client) {
        clients.put(client.getId(), client);
    }

    @Override
    public final void removeClient(T client) {
        clients.remove(client.getId());
    }

    @Override
    public final Collection<T> getClients() {
        return clients.values();
    }

    public final T getClient(int id) {
        return clients.get(id);
    }
}
