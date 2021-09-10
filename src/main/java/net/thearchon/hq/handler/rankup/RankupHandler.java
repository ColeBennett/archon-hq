package net.thearchon.hq.handler.rankup;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.NamedBukkitHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;

public class RankupHandler extends NamedBukkitHandler<RankupClient> {

    public RankupHandler(Archon archon) {
        super(archon);
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(RankupClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(RankupClient client) {

    }

    @Override
    protected void handleDisconnect(RankupClient client) {

    }
}
