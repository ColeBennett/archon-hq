package net.thearchon.hq.handler.uhc;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.GameState;
import net.thearchon.hq.util.io.FileUtil;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.hq.handler.GameHandler;

import java.io.File;

public class UhcHandler extends GameHandler<UhcClient> {
    
    private final File lobbySchem = new File(server.getDataFolder("uhc"), "uhclobby.schematic");
    
    public UhcHandler(Archon archon) {
        super(archon, ServerType.UHC_LOBBY);

//        for (int i = 1; i <= 30; i++) {
//            UhcClient client = new UhcClient(server, i, i <= 15 ? "SOLO" : "TEAM");
//            if (i <= 7) {
//                client.setSlots(24);
//            } else if (i > 7 && i <= 15) {
//                client.setSlots(48);
//            } else if (i > 15 && i <= 22) {
//                client.setSlots(24);
//            } else if (i > 22) {
//                client.setSlots(48);
//            }
//            addClient(client);
//        }
    }

    @Override
    public void requestReceived(Client client, final RequestPacket request, Protocol header, BufferedPacket data) {
        if (header == Protocol.GAME_REQUEST_JOIN) {
            Player player = server.getPlayer(data.getInt(0));
            if (player == null) {
                return;
            }
            if (data.hasIndex(1)) {
                UhcClient cl = getClient(data.getInt(1));
                boolean access = false;
                if (cl.getState() == GameState.WAITING) {
                    if (cl.getPlayerCount() < cl.getSlots()) {
                        access = true;
                    }
                } else if (cl.getState() == GameState.STARTING) {
                    if (cl.getPlayerCount() < cl.getSlots()) {
                        access = true;
                    }
                    if (player.hasPermission(Rank.VIP) && cl.getPlayerCount() >= cl.getSlots()) {
                        access = true;
                    }
                } else if (cl.getState() == GameState.IN_PROGRESS) {
                    if (player.hasPermission(Rank.HELPER)) {
                        access = true;
                    }
                }
                if (access) {
                    player.connect(cl);
                } else {
                    client.getChannel().respond(request, new BufferedPacket(1).writeBoolean(access));
                }
            } else {
                UhcClient cl = findFreeServer();
                client.getChannel().respond(request, cl != null ? new BufferedPacket(1).writeString("UHC" + cl.getId()) : new BufferedPacket(0));
            }
        }
    }

    @Override
    protected void handleInbound(UhcClient client, Protocol header, BufferedPacket buf) {
        if (header == Protocol.GAME_STATE_UPDATE) {
            if (buf.getEnum(0, GameState.class) == GameState.WAITING) {
                distribute(client, Protocol.GAME_SLOT_UPDATE, client.getSlots());
            }
        }
    }
    
    @Override
    protected void handleConnect(UhcClient client) {
        server.send(client, Protocol.GAME_DATA.construct(client.getMode(), client.getSlots(), FileUtil.fileToBytes(lobbySchem)));
    }

    @Override
    protected void handleDisconnect(UhcClient client) {

    }
}
