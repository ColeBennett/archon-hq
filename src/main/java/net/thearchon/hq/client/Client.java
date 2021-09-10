package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.nio.protocol.Packet;
import net.thearchon.nio.server.ClientListener;

import java.net.InetSocketAddress;

public class Client extends ClientAttributes {

    protected final Archon archon;

    /**
     * Type of client.
     */
    private final ServerType type;
    /**
     * Client is registered to its handler.
     */
    private boolean registered;
    /**
     * Netty channel of client.
     */
    private ClientListener channel;
    /**
     * Last time a packet was received from this client.
     */
    private long lastActivity;
    /**
     * Packets received from this client.
     */
    private long packetsReceived;
    /**
     * Packets sent to this client.
     */
    private long packetsSent;

    /**
     * Construct a new client.
     * @param archon the parent object
     * @param type type of client
     */
    public Client(Archon archon, ServerType type) {
        this(archon, type, null);
    }

    /**
     * Construct a new client.
     * @param archon the parent object
     * @param type type of client
     * @param channel channel of the client
     */
    public Client(Archon archon, ServerType type, ClientListener channel) {
        this.archon = archon;
        this.type = type;
        this.channel = channel;
    }
    
    public ServerType getType() {
        return type;
    }

    public boolean isRegistered() {
        return registered;
    }

    public ClientListener getChannel() {
        return channel;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public long getPacketsReceived() {
        return packetsReceived;
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }
    
    public void setChannel(ClientListener channel) {
        this.channel = channel;
        setLastActivity();
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public InetSocketAddress getRemoteAddress() {
        return channel != null ? channel.getRemoteAddress() : null;
    }

    public void send(Packet packet) {
        if (isActive()) {
            archon.send(channel, packet);
        }
    }

    public void send(Iterable<Packet> packets) {
        for (Packet packet : packets) {
            send(packet);
        }
    }

    public void addPacketReceived() {
        packetsReceived++;
        setLastActivity();
    }

    public void addPacketSent() {
        packetsSent++;
        setLastActivity();
    }

    public void setLastActivity() {
        lastActivity = System.currentTimeMillis();
    }
}
