package net.thearchon.hq.handler;

import net.thearchon.hq.client.Client;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.hq.Archon;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class PriorityHandler<T extends Client> extends AbstractHandler<T> {
    
    private final Set<PacketHandler<T>> packetHandlers = new LinkedHashSet<>();
    private final Set<ConnectHandler<T>> connectHandlers = new LinkedHashSet<>();
    private final Set<DisconnectHandler<T>> disconnectHandlers = new LinkedHashSet<>();
    
    public PriorityHandler(Archon server) {
        super(server);
    }

    @Override
    public final void packetReceived(T client, Protocol header, BufferedPacket buf) {
        for (PacketHandler<T> handler : packetHandlers) {
            handler.packetReceived(client, header, buf);
        }
        handleInbound(client, header, buf);
    }
    
    @Override
    public final void clientConnected(T client) {
        for (ConnectHandler<T> handler : connectHandlers) {
            handler.clientConnected(client);
        }
        handleConnect(client);
    }

    @Override
    public final void clientDisconnected(T client) {
        for (DisconnectHandler<T> handler : disconnectHandlers) {
            handler.clientDisconnected(client);
        }
        handleDisconnect(client);
    }

    protected abstract void handleInbound(T client, Protocol header, BufferedPacket buf);

    protected abstract void handleConnect(T client);

    protected abstract void handleDisconnect(T client);

    public interface PacketHandler<C extends Client> {
        void packetReceived(C client, Protocol header, BufferedPacket buf);
    }

    public interface ConnectHandler<C extends Client> {
        void clientConnected(C client);
    }

    public interface DisconnectHandler<C extends Client> {
        void clientDisconnected(C client);
    }

    public boolean addHandler(PacketHandler<T> handler) {
        return packetHandlers.add(handler);
    }

    public boolean addHandler(ConnectHandler<T> handler) {
        return connectHandlers.add(handler);
    }

    public boolean addHandler(DisconnectHandler<T> handler) {
        return disconnectHandlers.add(handler);
    }

    public boolean removeHandler(PacketHandler<T> handler) {
        return packetHandlers.remove(handler);
    }

    public boolean removeHandler(ConnectHandler<T> handler) {
        return connectHandlers.remove(handler);
    }

    public boolean removeHandler(DisconnectHandler<T> handler) {
        return disconnectHandlers.remove(handler);
    }
}
