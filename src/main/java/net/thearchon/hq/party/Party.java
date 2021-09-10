package net.thearchon.hq.party;

import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.client.BukkitClient;

import java.util.HashSet;
import java.util.Set;

public class Party {

    private Player host;
    private final Set<Player> members = new HashSet<>();
    private int maxPlayers;

    public Party() {

    }

    public Player getHost() {
        return host;
    }

    public Set<Player> getMembers() {
        return members;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setHost(Player player) {
        host = player;

        Rank rank = player.getRank();
        if (rank == Rank.VIP) {
            maxPlayers = 5;
        } else if (rank == Rank.VIP_PLUS) {
            maxPlayers = 10;
        } else if (rank == Rank.MVP) {
            maxPlayers = 15;
        } else if (Rank.hasPermission(rank, Rank.MVP_PLUS)) {
            maxPlayers = 20;
        }
    }

    public void addMember(Player player) {
        members.add(player);
    }

    public void removeMember(Player player) {
        members.remove(player);
    }

    public boolean hasMember(Player player) {
        return members.contains(player);
    }

    public boolean isHost(Player player) {
        return host == player;
    }

    public int getPlayerCount() {
        return members.size() + 1;
    }

    public void message(String message) {
        host.message(message);
        for (Player player : members) {
            player.message(message);
        }
    }

    public void send(BukkitClient client) {
        host.connect(client);
        for (Player player : members) {
            player.connect(client);
        }
    }

    public void end() {

    }
}
