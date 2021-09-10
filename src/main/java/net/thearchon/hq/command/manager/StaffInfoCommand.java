package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.command.ConsoleSender;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffInfoCommand extends Command {

    private final String topStaffSql = "SELECT COUNT(*) AS actions," +
            " (SELECT name FROM players WHERE players.id = punisher) AS name," +
            " (SELECT rank FROM players WHERE players.id = punisher) AS rank," +
            " MONTHNAME(time) AS month" +
            " FROM punishment_log WHERE MONTH(time) = MONTH(CURDATE()) && YEAR(time) = YEAR(CURDATE())" +
            " GROUP BY punisher ORDER BY actions DESC LIMIT 7;";
    private final String topStaffByRankSql = "SELECT COUNT(*) AS actions," +
            " (SELECT name FROM players WHERE players.id = punisher) AS name," +
            " MONTHNAME(time) AS month" +
            " FROM punishment_log WHERE MONTH(time) = MONTH(CURDATE()) && YEAR(time) = YEAR(CURDATE())" +
            " && (SELECT rank FROM players WHERE players.id = punisher) = '%s'" +
            " GROUP BY punisher ORDER BY actions DESC LIMIT 7;";
    private final String staffInfoSql = "SELECT COUNT(*) AS actions, type, " +
            " MONTHNAME(time) AS month" +
            " FROM punishment_log WHERE punisher = %d" +
            " && MONTH(time) = MONTH(CURDATE()) && YEAR(time) = YEAR(CURDATE())" +
            " GROUP BY type ORDER BY actions DESC;";

    public StaffInfoCommand(Archon archon) {
        super(archon, "/staffinfo [player/rank]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            player.error(getSyntax());
            return;
        }

        if (args.length == 0) {
            player.message(Message.SEARCHING);
            archon.getDataSource().getConnection(conn -> {
                List<String> msg = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(topStaffSql)) {
                    int idx = 1;
                    while (rs.next()) {
                        if (msg.isEmpty()) {
                            msg.add("&6Top Active Staff &7- &a" + rs.getString("month"));
                        }
                        String name = rs.getString("name");
                        String rank;
                        if (name == null) {
                            name = ConsoleSender.INSTANCE.getName();
                            rank = Rank.OWNER.getDisplay();
                        } else {
                            rank = Rank.valueOf(rs.getString("rank")).getDisplay();
                        }
                        msg.add("&7" + idx++ + ": &e" + name + " &7- &3" + Util.addCommas(rs.getInt("actions")) + " &7actions &7- " + rank);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                archon.runTask(() -> player.message(msg));
            });
        } else if (args.length == 1) {
            String input = args[0];
            try {
                Rank rank = Rank.valueOf(input.toUpperCase());
                player.message("&7Searching by " + rank.getDisplay() + "&7, please wait...");
                archon.getDataSource().getConnection(conn -> {
                    List<String> msg = new ArrayList<>();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(String.format(topStaffByRankSql, rank.name()))) {
                        int idx = 1;
                        while (rs.next()) {
                            if (msg.isEmpty()) {
                                msg.add("&6Top Active Staff &7- " + rank.getDisplay() + " &7- &a" + rs.getString("month"));
                            }
                            String name = rs.getString("name");
                            if (name == null) {
                                name = "&c&oArchonHQ";
                            }
                            msg.add("&7" + idx++ + ": &e" + name + " &7- &3" + Util.addCommas(rs.getInt("actions")) + " &7actions");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    archon.runTask(() -> player.message(msg));
                });
            } catch (IllegalArgumentException ex) {
                PlayerInfo info = archon.getPlayerInfo(input);
                if (info == null) {
                    player.error(Message.PLAYER_NOT_FOUND.format(input));
                    return;
                }

                player.message(Message.SEARCHING);
                archon.getDataSource().getConnection(conn -> {
                    List<String> msg = new ArrayList<>();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(String.format(staffInfoSql, info.getId()))) {
                        int idx = 1;
                        while (rs.next()) {
                            if (msg.isEmpty()) {
                                msg.add("&6Staff Activity &7- &e" + info.getName() + " &7- &a" + rs.getString("month"));
                            }
                            msg.add("&7" + idx++ + ": &c" + rs.getString("type") + " &7- &3" + Util.addCommas(rs.getInt("actions")) + " &7actions");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    archon.runTask(() -> player.message(msg));
                });
            }
        }
    }
}
