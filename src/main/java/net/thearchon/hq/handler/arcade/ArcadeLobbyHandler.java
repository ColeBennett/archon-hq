package net.thearchon.hq.handler.arcade;

import net.thearchon.hq.Archon;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

public class ArcadeLobbyHandler extends GameLobbyHandler<ArcadeLobbyClient, ArcadeClient> {

    public ArcadeLobbyHandler(Archon archon, ArcadeHandler gameHandler) {
        super(archon, gameHandler);

//        for (int i = 1; i <= 3; i++) {
//            addClient(new ArcadeLobbyClient(server, i));
//        }
    }

    @Override
    protected void handleInbound(ArcadeLobbyClient client, Protocol header, BufferedPacket buf) {

    }

    @Override
    protected void handleConnect(ArcadeLobbyClient client) {

    }

    @Override
    protected void handleDisconnect(ArcadeLobbyClient client) {

    }
}
