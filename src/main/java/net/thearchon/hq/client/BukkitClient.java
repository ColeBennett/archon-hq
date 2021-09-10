package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.Protocol;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class BukkitClient extends MonitorableClient {

    private final String serverName;

    private int slots;
    private float tps;
    private boolean locked;
    private String lockMessage;

    private final Set<Player> players = new LinkedHashSet<>();

    public BukkitClient(Archon archon, ServerType type, String serverName) {
        super(archon, type);

        this.serverName = serverName;
    }

    public void addPlayer(Player player) {
        if (players.add(player)) {
            updateTime();
        }
    }

    public void removePlayer(Player player) {
        if (players.remove(player)) {
            updateTime();
        }
    }

    public String getServerName() {
        return serverName;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }

    public int getOnlineCount() {
        return players.size();
    }

    public int getSlots() {
        return slots;
    }

    public float getTps() {
        return tps;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getLockMessage() {
        return lockMessage;
    }

    public void setSlots(int slots) {
        this.slots = slots;
        updateTime();
    }

    public void setTps(float tps) {
        this.tps = tps;
        updateTime();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        updateTime();
    }

    public void setLockMessage(String lockMessage) {
        this.lockMessage = lockMessage;
        updateTime();
    }

    public void runConsoleCommand(String command) {
        send(Protocol.EXEC_COMMAND.construct(command));
    }

    public void runConsoleCommand(String... commands) {
        send(Protocol.EXEC_COMMAND.construct((Object[]) commands));
    }

    public void broadcast(Message message) {
        broadcast(message.toString());
    }

    public void broadcast(String message) {
        send(Protocol.BROADCAST.construct(message));
    }

    public void broadcast(String... messages) {
        send(Protocol.BROADCAST.construct((Object[]) messages));
    }

    public void broadcast(List<String> messages) {
        send(Protocol.BROADCAST.construct(messages));
    }

    public void removePlayers() {
        LobbyHandler handler = archon.getHandler(ServerType.LOBBY);
        for (Player player : players) {
            LobbyClient lobby = handler.availableServer(player.getRegion());
            if (lobby != null) {
                player.connect(lobby);
            } else {
                player.disconnect("&cThere are no available lobbies to connect to.\n&cTry relogging in a few seconds.");
            }
        }
    }

    public String getDisplayName() {
        return Util.upperLower(serverName);
    }

    @Override
    public void reset() {
        slots = 0;
        tps = 0;
        locked = false;
        super.reset();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        return o instanceof BukkitClient
                && ((BukkitClient) o).getServerName().equalsIgnoreCase(serverName);
    }
}
