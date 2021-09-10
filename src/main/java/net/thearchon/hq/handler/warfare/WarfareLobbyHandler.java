package net.thearchon.hq.handler.warfare;

import net.thearchon.hq.Archon;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

public class WarfareLobbyHandler extends GameLobbyHandler<WarfareLobbyClient, WarfareClient> {

    public WarfareLobbyHandler(Archon archon, WarfareHandler gameHandler) {
        super(archon, gameHandler);
    }

    @Override
    protected void handleInbound(WarfareLobbyClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(WarfareLobbyClient client) {

    }

    @Override
    protected void handleDisconnect(WarfareLobbyClient client) {

    }
}
