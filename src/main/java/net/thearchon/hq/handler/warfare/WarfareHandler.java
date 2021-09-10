package net.thearchon.hq.handler.warfare;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.GameHandler;
import net.thearchon.hq.handler.GameState;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.protocol.stream.ChunkedFilePacket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarfareHandler extends GameHandler<WarfareClient> {

    private final File schemDir;
    private final Random random = new Random();

    public WarfareHandler(Archon archon) {
        super(archon, ServerType.SKYWARS_LOBBY);

        schemDir = new File(archon.getDataFolder("skywars"), "schematics");
        schemDir.mkdir();
    }

    @Override
    public void requestReceived(Client client, final RequestPacket request, Protocol header, BufferedPacket buf) {
        if (header == Protocol.GAME_REQUEST_JOIN) {
            Player player = server.getPlayer(buf.getInt(0));
            if (player == null) {
                return;
            }
            if (buf.hasIndex(1)) {
                WarfareClient cl = getClient(buf.getInt(1));
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
                    access = true;
                }
                if (access) {
                    player.connect(cl);
                } else {
                    client.getChannel().respond(request, new BufferedPacket(1).writeBoolean(access));
                }
            } else {
                WarfareClient cl = findFreeServer();
                client.getChannel().respond(request, cl != null ? new BufferedPacket(1).writeString("skywars" + cl.getId()) : new BufferedPacket(0));
            }
        }
    }

    @Override
    protected void handleInbound(WarfareClient client, Protocol header, BufferedPacket buf) {
        if (header == Protocol.GAME_WIN) {
            Player player = server.getPlayer(buf.getInt(0));
            if (player != null) {
                WarfareLobbyHandler handler = server.getHandler(ServerType.SKYWARS_LOBBY);
                handler.sendAll(Protocol.BROADCAST.construct(player.getDisplayName() + " &7has won on &a" + buf.getString(1) + "&7!"));
            }
        }
    }

    @Override
    protected void handleConnect(WarfareClient client) {
        File[] files = schemDir.listFiles();
        List<File> names = new ArrayList<File>(files.length);
        for (File file : files) {
            if (file.getName().endsWith(".schematic")) {
                names.add(file);
            }
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("No schematics found: " + schemDir);
        }
        server.send(client, new ChunkedFilePacket(names.get(random.nextInt(names.size()))));
    }

    @Override
    protected void handleDisconnect(WarfareClient client) {

    }
}
