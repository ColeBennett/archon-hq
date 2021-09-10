package net.thearchon.hq.handler.setup;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerRegion;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.BukkitHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;

import java.util.Collection;
import java.util.Collections;

public class SetupHandler extends BukkitHandler<SetupClient> {

    private SetupClient client;

    public SetupHandler(Archon archon) {
        super(archon);

        client = new SetupClient(archon);
        client.setRegion(ServerRegion.NA);
        client.setIpAddress("");
        client.setDatacenter("");
    }

    @Override
    public SetupClient register(ClientListener channel, BufferedPacket buf) {
        return client;
    }

    @Override
    public Collection<SetupClient> getClients() {
        return Collections.singleton(client);
    }

    @Override
    public void requestReceived(Client client, RequestPacket request, Protocol header, BufferedPacket buf) {

    }

    @Override
    public void addClient(SetupClient client) {
        this.client = client;
    }

    @Override
    public void removeClient(SetupClient client) {
        client = null;
    }

    @Override
    protected void handleInbound(SetupClient client, Protocol header, BufferedPacket buf) {

    }

    @Override
    protected void handleConnect(SetupClient client) {

    }

    @Override
    protected void handleDisconnect(SetupClient client) {

    }
}
