package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;
import net.thearchon.nio.protocol.Packet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractHandler<T extends Client> implements Handler<T> {

    protected final Archon server;

    private final Set<T> active = new HashSet<>();

    public AbstractHandler(Archon archon) {
        this.server = archon;
    }

    @Override
    public void sendAll(Packet packet) {
        for (T client : getActiveClients()) {
            client.send(packet);
        }
    }

    @Override
    public final Set<T> getActiveClients() {
        return active;
    }

    @Override
    public int getClientCount() {
        Collection<T> clients = getClients();
        return clients != null ? clients.size() : 0;
    }

    @Override
    public final int getActiveClientCount() {
        Set<T> active = getActiveClients();
        return active != null ? active.size() : 0;
    }
}
