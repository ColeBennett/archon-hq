package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.server.ClientListener;
import net.thearchon.hq.client.BukkitClient;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class NamedBukkitHandler<T extends BukkitClient> extends BukkitHandler<T> {
    
    private final Map<String, T> clients = new LinkedHashMap<>();
    
    public NamedBukkitHandler(Archon archon) {
        super(archon);

        addHandler((ConnectHandler<T>) client ->
                Message.notifyServerStatus(archon, client, true));
        addHandler((DisconnectHandler<T>) client ->
                Message.notifyServerStatus(archon, client, false));
    }
    
    @Override
    public final T register(ClientListener channel, BufferedPacket buf) {
        String serverName = buf.getString(0);
        T client = clients.get(serverName);
        if (client == null) {
            channel.close();
            server.getLogger().warning("Found unregistered " + getClass().getSimpleName()
                    + ": " + serverName + ", " + clients.keySet());
            return null;
        }
        client.setSlots(buf.getInt(1));
        return client;
    }

    @Override
    public final Collection<T> getClients() {
        return clients.values();
    }

    public final T getClient(String serverName) {
        return clients.get(serverName);
    }
    
    public final void addClient(T client) {
        clients.put(client.getServerName(), client);
    }

    public final void removeClient(T client) {
        clients.remove(client.getServerName());
    }
}
