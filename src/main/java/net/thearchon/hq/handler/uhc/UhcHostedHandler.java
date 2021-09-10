package net.thearchon.hq.handler.uhc;

import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.IdBukkitClient;
import net.thearchon.hq.handler.IdBukkitHandler;
import net.thearchon.hq.handler.uhc.UhcHostedHandler.UhcHostedClient;

public class UhcHostedHandler extends IdBukkitHandler<UhcHostedClient> {

    public UhcHostedHandler(Archon archon) {
        super(archon);
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(UhcHostedClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(UhcHostedClient client) {
        client.setSlots(100);
    }

    @Override
    protected void handleDisconnect(UhcHostedClient client) {

    }

    public static final class UhcHostedClient extends IdBukkitClient {
        public UhcHostedClient(Archon archon, int id) {
            super(archon, ServerType.UHC_HOSTED, "hosteduhc", id);
        }
    }
}
