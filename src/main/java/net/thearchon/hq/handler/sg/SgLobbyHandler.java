package net.thearchon.hq.handler.sg;

import net.thearchon.hq.Archon;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

public class SgLobbyHandler extends GameLobbyHandler<SgLobbyClient, SgClient> {

    public SgLobbyHandler(Archon archon, SgHandler gameHandler) {
        super(archon, gameHandler);
    }

    @Override
    protected void handleInbound(SgLobbyClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(SgLobbyClient client) {

    }

    @Override
    protected void handleDisconnect(SgLobbyClient client) {

    }
}
