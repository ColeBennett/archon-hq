package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.GameClient;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.stream.ChunkedFilePacket;

import java.io.File;
import java.util.*;

public abstract class GameHandler<T extends GameClient> extends IdBukkitHandler<T> {

    private final ServerType lobbyType;

    public GameHandler(Archon archon, ServerType lobbyType) {
        super(archon);

        this.lobbyType = lobbyType;

        addHandler((client, header, buf) -> {
            if (header == Protocol.GAME_PLAYER_COUNT_UPDATE) {
                client.setPlayerCount(buf.getInt(0));
                distribute(client, header, client.getPlayerCount());
            } else if (header == Protocol.GAME_SPECTATOR_COUNT_UPDATE) {
                client.setSpectatorCount(buf.getInt(0));
                distribute(client, header, client.getSpectatorCount());
            } else if (header == Protocol.GAME_SLOT_UPDATE) {
                client.setSlots(buf.getInt(0));
                distribute(client, header, client.getSlots());
            } else if (header == Protocol.GAME_STARTING_COUNT_UPDATE) {
                client.setStartingCount(buf.getInt(0));
                distribute(client, header, client.getStartingCount());
            } else if (header == Protocol.GAME_STATE_UPDATE) {
                GameState state = GameState.valueOf(buf.getString(0));
                client.setState(state);
                if (state == GameState.RESTARTING) {
                    restarting(client);
                }
                distribute(client, header, state);
            } else if (header == Protocol.GAME_MAP_NAME_UPDATE) {
                client.setMapName(buf.getString(0));
                distribute(client, header, client.getMapName());
            } else if (header == Protocol.GAME_GET_MAP_FILE) {
                File f = new File("data/" + buf.getString(0));
                if (f.isDirectory()) {
                    File[] list = f.listFiles();
                    List<File> names = new ArrayList<>(list.length);
                    for (File file : list) {
                        if (file.getName().endsWith(".zip")) {
                            names.add(file);
                        }
                    }
                    if (names.isEmpty()) {
                        throw new IllegalStateException("No map files found: " + f.getAbsolutePath());
                    }
                    client.send(new ChunkedFilePacket(names.get(Util.random().nextInt(names.size()))));
                } else {
                    if (!f.getName().endsWith(".zip")) {
                        throw new IllegalStateException("Map file must be a zip");
                    }
                    client.send(new ChunkedFilePacket(f));
                }
            }
        });
        addHandler((ConnectHandler<T>) client -> {
            client.setState(GameState.LOADING);
            distribute(client, Protocol.GAME_STATE_UPDATE, client.getState());
        });
        addHandler((DisconnectHandler<T>) client -> {
            restarting(client);
            client.setState(GameState.RESTARTING);
            distribute(client, Protocol.GAME_STATE_UPDATE, client.getState());
        });
    }

    public ServerType getLobbyType() {
        return lobbyType;
    }

    public void sendServerList(Client lobby) {
        BufferedPacket buf = Protocol.GAME_SERVER_LIST.buffer(getClients().size() * 6);
        for (T client : getClients()) {
            buf.writeInt(client.getId());
            buf.writeEnum(client.getState());
            buf.writeInt(client.getPlayerCount());
            buf.writeInt(client.getSpectatorCount());
            buf.writeInt(client.getSlots());
            buf.writeString(client.getMapName());
        }
        lobby.send(buf);
    }

    public void distribute(T client, Protocol header) {
        distribute(buffer(client, header));
    }

    public void distribute(T client, Protocol header, Object... values) {
        distribute(buffer(client, header, values));
    }

    public void distribute(BufferedPacket packet) {
        server.sendAll(packet, lobbyType);
    }

    public BufferedPacket buffer(T client, Protocol header) {
        return header.construct(client.getId());
    }

    public BufferedPacket buffer(T client, Protocol header, Object... values) {
        return header.buffer(values.length + 1)
                .writeInt(client.getId())
                .writeAll(values);
    }

    public T findFreeServer() {
        Map<T, Integer> counts = new LinkedHashMap<>();
        for (T server : getActiveClients()) {
            if (server.getState() == GameState.WAITING
                    && server.getPlayerCount() < server.getSlots()) {
                counts.put(server, server.getPlayerCount());
            }
        }
        if (!counts.isEmpty()) {
            return Util.sortedValueKeys(counts).get(0);
        }
        return null;
    }

    private void restarting(T client) {
        if (client.getPlayerCount() != 0) {
            client.setPlayerCount(0);
            distribute(client, Protocol.GAME_PLAYER_COUNT_UPDATE, 0);
        }
        if (client.getSpectatorCount() != 0) {
            client.setSpectatorCount(0);
            distribute(client, Protocol.GAME_SPECTATOR_COUNT_UPDATE, 0);
        }
        if (client.getSlots() != 0) {
            client.setSlots(0);
            distribute(client, Protocol.GAME_SLOT_UPDATE, 0);
        }
        if (client.getMapName() != null) {
            client.setMapName(null);
            distribute(client, Protocol.GAME_MAP_NAME_UPDATE, new Object[] {null});
        }
    }
}
