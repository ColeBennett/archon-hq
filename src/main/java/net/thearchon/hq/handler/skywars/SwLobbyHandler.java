package net.thearchon.hq.handler.skywars;

import net.thearchon.hq.Archon;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

public class SwLobbyHandler extends GameLobbyHandler<SwLobbyClient, SwClient> {

    public SwLobbyHandler(Archon archon, SwHandler gameHandler) {
        super(archon, gameHandler);
    }

    @Override
    protected void handleInbound(SwLobbyClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(SwLobbyClient client) {

    }

    @Override
    protected void handleDisconnect(SwLobbyClient client) {

    }
}
