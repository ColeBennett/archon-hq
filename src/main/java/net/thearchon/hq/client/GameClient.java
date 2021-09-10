package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.handler.GameState;

public abstract class GameClient extends IdBukkitClient {

    private String mapName;
    private int playerCount;
    private int spectatorCount;
    private int slots;
    private int startingCount;
    private GameState state;

    public GameClient(Archon archon,
            ServerType type, String prefix, int id) {
        super(archon, type, prefix, id);

        state = GameState.OFFLINE;
    }

    public String getMapName() {
        return mapName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getSpectatorCount() {
        return spectatorCount;
    }

    public int getSlots() {
        return slots;
    }

    public int getStartingCount() {
        return startingCount;
    }

    public GameState getState() {
        return state;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public void setSpectatorCount(int spectatorCount) {
        this.spectatorCount = spectatorCount;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public void setStartingCount(int startingCount) {
        this.startingCount = startingCount;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
