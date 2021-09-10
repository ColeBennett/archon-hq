package net.thearchon.hq.handler.event;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.NamedBukkitHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;

public class EventsHandler extends NamedBukkitHandler<EventsClient> {

    public EventsHandler(Archon archon) {
        super(archon);
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(EventsClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(EventsClient client) {

    }

    @Override
    protected void handleDisconnect(EventsClient client) {

    }
}
