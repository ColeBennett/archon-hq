package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;

import java.util.LinkedHashSet;
import java.util.Set;

public class BungeeClient extends MonitorableClient {
    
    private final int id;
    private final String datacenter;
    private final Set<Player> players = new LinkedHashSet<>();

    public BungeeClient(Archon archon, int id, String datacenter) {
        super(archon, ServerType.BUNGEE);

        this.id = id;
        this.datacenter = datacenter;
    }
    
    public int getId() {
        return id;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public int getOnlineCount() {
        return players.size();
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }

    public void addPlayer(Player player) {
        if (players.add(player)) {
            updateTime();
        } else {
            archon.getLogger().warning("Cannot re-add player to bungee " + id + ": " + player.getName());
        }
    }

    public void removePlayer(Player player) {
        if (players.remove(player)) {
            updateTime();
        } else {
            archon.getLogger().warning("Cannot remove player from bungee " + id + ": " + player.getName());
        }
    }
}
