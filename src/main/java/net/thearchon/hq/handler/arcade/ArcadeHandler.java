package net.thearchon.hq.handler.arcade;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.handler.GameHandler;

public class ArcadeHandler extends GameHandler<ArcadeClient> {

    public ArcadeHandler(Archon archon) {
        super(archon, ServerType.ARCADE_LOBBY);
        
//        ArcadeGameType[] types = ArcadeGameType.values();
//        int idx = 0;
//        for (int i = 1; i <= 90; i++) {
//            ArcadeClient client = new ArcadeClient(server, i);
//            client.setGameType(types[idx++]);
//            if (idx >= types.length) {
//                idx = 0;
//            }
//            addClient(client);
//        }
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(ArcadeClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(ArcadeClient client) {
        client.send(Protocol.CONNECT.buffer(1).writeEnum(client.getGameType()));
    }

    @Override
    protected void handleDisconnect(ArcadeClient client) {

    }

    @Override
    public void sendServerList(Client lobby) {
        BufferedPacket buf = Protocol.GAME_SERVER_LIST.buffer(getClients().size() * 7);
        for (ArcadeClient client : getClients()) {
            buf.writeInt(client.getId());
            buf.writeEnum(client.getState());
            buf.writeInt(client.getPlayerCount());
            buf.writeInt(client.getSpectatorCount());
            buf.writeInt(client.getSlots());
            buf.writeString(client.getMapName());
            buf.writeEnum(client.getGameType());
        }
        lobby.send(buf);
    }
}
