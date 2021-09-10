package net.thearchon.hq.handler.sg;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.GameState;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.FilePacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.handler.GameHandler;

import java.io.File;

public class SgHandler extends GameHandler<SgClient> {

    public SgHandler(Archon archon) {
        super(archon, ServerType.SG_LOBBY);

        new File("data/sg/maps").mkdir();
    }

    @Override
    public void requestReceived(Client client, RequestPacket request, Protocol header, BufferedPacket buf) {
        if (header == Protocol.GAME_REQUEST_JOIN) {
            Player player = server.getPlayer(buf.getInt(0));
            if (player == null) {
                return;
            }
            if (buf.hasIndex(1)) {
                SgClient cl = getClient(buf.getInt(1));
                boolean access = false;
                if (cl.getState() == GameState.WAITING) {
                    if (cl.getPlayerCount() < cl.getSlots()) {
                        access = true;
                    }
                } else if (cl.getState() == GameState.STARTING) {
                    if (cl.getStartingCount() > 10) {
                        if (cl.getPlayerCount() < cl.getSlots()) {
                            access = true;
                        }
                        if (player.hasPermission(Rank.VIP) && cl.getPlayerCount() >= cl.getSlots()) {
                            access = true;
                        }
                    }
                } else if (cl.getState() == GameState.IN_PROGRESS) {
                    access = true;
                }
                if (access) {
                    player.connect(cl);
                } else {
                    client.getChannel().respond(request, new BufferedPacket(1).writeBoolean(access));
                }
            } else {
                SgClient cl = findFreeServer();
                client.getChannel().respond(request, cl != null ? new BufferedPacket(1).writeString("SG" + cl.getId()) : new BufferedPacket(0));
            }
        }
    }

    @Override
    protected void handleInbound(SgClient client, Protocol header, BufferedPacket buf) {

    }

    @Override
    protected void handleConnect(SgClient client) {
        client.send(new FilePacket(
                new File("data/sg/deathmatch.zip"),
                new BufferedPacket(0)));
    }

    @Override
    protected void handleDisconnect(SgClient client) {

    }
}
