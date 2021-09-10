package net.thearchon.hq.command.manager;

import net.thearchon.hq.*;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.language.Message;
import net.thearchon.nio.Protocol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RankCommand extends Command {

    public RankCommand(Archon archon) {
        super(archon, "/rank <player> <rank> [server]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2 || args.length > 3) {
            player.error(getSyntax());
            return;
        }
        Rank newRank = Rank.valueOf(args[1].toUpperCase());
        if (newRank == null) {
            player.error("Rank not found: " + args[1]);
            return;
        }
        if (newRank.hasPermission(Rank.ADMIN) && !player.hasPermission(Rank.ADMIN)) {
            player.error("You do not have permission to set players to " + newRank.getName() + '.');
            return;
        }

        PlayerInfo info = archon.getPlayerInfo(args[0]);
        if (info != null) {
            String name = info.getName();
            Rank rank = null;

            try (Connection conn = archon.getDataSource().getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT rank FROM players WHERE id = " + info.getId())) {
                 if (rs.next()) {
                    rank = Rank.valueOf(rs.getString("rank"));
                 }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (rank == null) {
                player.error("Unable to find player rank: " + info.getName());
                return;
            }

            String server = args.length == 3 ? args[2] : null;
            if (server != null) {
                if (!server.contains("faction")) {
                    server = "faction" + server;
                }
                BukkitClient client = archon.getBukkitClient(server);
                if (client == null) {
                    player.error(Message.SERVER_NOT_FOUND.format(server));
                    return;
                }
                if (!client.isActive()) {
                    player.error(Message.SERVER_OFFLINE.format(client.getServerName()));
                    return;
                }
                String group = newRank.getName().replace("-", "");
                if (newRank == Rank.DEFAULT) {
                    for (Rank r : Rank.values()) {
                        if (r.hasPermission(Rank.HELPER)) {
                            client.runConsoleCommand("perm player " + info.getUuid() + " removegroup " + r.getName().replace("-", ""));
                        }
                    }
                    player.message("&cRemoved &7" + info.getName() + "'s &c" + group + " rank &aon &7" + client.getServerName() + "&c.");
                } else {
                    client.runConsoleCommand("perm player " + info.getUuid() + " addgroup " + group);
                    player.message("&aGave &7" + info.getName() + " &6" + group + " &aon &7" + client.getServerName() + "&a.");
                }
            }

            archon.getDataSource().execute("UPDATE players SET rank = ? WHERE id = ?;", newRank.name(), info.getId());

            Player p = archon.getPlayerByName(name);
            if (p != null) {
                p.setRank(newRank);
                p.getCurrentServer().send(Protocol.RANK_UPDATE.construct(p.getId(), newRank.name()));
                if (Rank.hasPermission(rank, Rank.HELPER) && !Rank.hasPermission(newRank, Rank.HELPER)) {
                    archon.sendAll(Protocol.ENABLE_CHAT.construct(p.getUuid()), ServerType.BUNGEE);
                }
            }

            archon.notifyStaff(player.getDisplayName() + " &7set " + rank.getColor() + name + "'s &7rank to " + newRank.getPrefix().replaceAll("\\[|\\]", "").trim() + "&7!");
        } else {
            player.error("Player not found in database: " + args[0]);
        }
    }
}
