package net.thearchon.hq.handler;

import net.thearchon.hq.client.Client;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.Packet;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;

import java.util.Collection;
import java.util.Set;

/**
 * Top-level interface for all handlers. Handlers are used to handle
 * events fired by registered clients, and serve as an area to group
 * clients based on their specific type.
 * @param <T> type of client that applies to related methods.
 */
public interface Handler<T extends Client> {

    /**
     * Register an incoming channel to this handler. Return null
     * to stop the client from successfully connecting and being
     * registered if needed.
     * @param channel nio channel that is connecting
     * @param buf initial login packet received by the channel
     * @return newly constructed client to be added to this handler
     */
    T register(ClientListener channel, BufferedPacket buf);

    /**
     * Handle an inbound request from a client that has been
     * registered to this handler. Respond to the client by
     * using its respond method. Input the request object.
     * @param client client that sent the request
     * @param request request that can be responded to
     * @param header protocol header of the buffer
     * @param buf data received in the request
     */
    void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf);

    /**
     * Handle an inbound packet from a client that has been
     * registered to this handler.
     * @param client client that sent the packet
     * @param header protocol header of the buffer
     * @param buf data received in the packet
     */
    void packetReceived(T client, Protocol header, BufferedPacket buf);

    /**
     * Notified only when a client has been successfully
     * registered into this handler.
     * @param client client that connected
     */
    void clientConnected(T client);

    /**
     * Notified when a client has been disconnected from the archon.
     * Packets sent to this client in and after this method will be discarded,
     * as it will already have been unregistered as a client.
     * @param client client that disconnected
     */
    void clientDisconnected(T client);

    /**
     * Send a packet to all active clients registered to this handler.
     * @param packet packet to send
     */
    void sendAll(Packet packet);

    /**
     * Add a client to this handler.
     * @param client client to add
     */
    void addClient(T client);

    /**
     * Remove a client from this handler.
     * @param client client to remove
     */
    void removeClient(T client);

    /**
     * Returns a list of all clients added to this handler. This returns
     * all active and non-active clients.
     * @return collection of clients
     */
    Collection<T> getClients();

    /**
     * Returns a set of all active clients registered to this handler.
     * @return set of active clients
     */
    Set<T> getActiveClients();

    /**
     * Returns the number of clients added to this handler.
     * @return number of clients
     */
    int getClientCount();

    /**
     * Returns the number of active clients registered to this handler.
     * @return number of active clients
     */
    int getActiveClientCount();
}
