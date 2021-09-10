package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.punish.Punishment;
import net.thearchon.hq.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends Command {

    public InfoCommand(Archon archon) {
        super(archon, "/info <player> [filter: kick, tempban, ban, unban]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.error(getSyntax());
            return;
        }

        String input = args[0];
        if (input.equalsIgnoreCase("SimpleRansacking")) return;

        archon.getPlayerInfo(input, info -> {
            if (info == null) {
                player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
                return;
            }

            String sql;
            if (args.length == 2) {
                Punishment filter = null;
                try {
                    filter = Punishment.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.error("Filter not found: " + args[1] + ". Available filters: kick, tempban, ban, unban");
                    return;
                }
                sql = "SELECT (SELECT name FROM players WHERE players.id = punisher) AS punisher_name, type, DATE(time) AS date, duration, reason FROM punishment_log WHERE punished = ? && type = '" + filter.name() + "'";
            } else {
                sql = "SELECT (SELECT name FROM players WHERE players.id = punisher) AS punisher_name, type, DATE(time) AS date, duration, reason FROM punishment_log WHERE punished = ?";
            }

            player.message("&aSearching, please wait...");
            archon.getDataSource().getConnection(conn -> {
                List<String> msg = new ArrayList<>();
                msg.add("&7--- &6" + info.getName() + "'s &aRecord &7---");
                int count = 0;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql.replace("?", "" + info.getId()))) {
                    while (rs.next()) {
                        Punishment type = Punishment.valueOf(rs.getString("type"));
                        String punisher = rs.getString("punisher_name");
                        if (punisher == null) {
                            punisher = "Console";
                        }
                        String line = "&7" + displayName(type) + " by &c" + punisher;
                        String reason = rs.getString("reason");
                        if (reason != null) {
                            line += " &7with reason &f&o" + reason;
                        }
                        Long duration = rs.getLong("duration");
                        if (duration != null && duration != 0) {
                            line += " &7for &a" + DateTimeUtil.formatTime(duration / 1000);
                        }

                        line += " &8&o(" + rs.getString("date") + ")";

                        count++;
                        msg.add("&4" + count + ". " + line);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (count == 0) {
                    player.message("&cNo results found.");
                } else {
                    msg.add("&e&oDisplaying " + count + " result(s)");
                    player.message(msg);
                }
            });
        });
    }

    private String displayName(Punishment punishment) {
        switch (punishment) {
            case KICK:
                return "Kicked";
            case MUTE:
                return "Muted";
            case TEMPBAN:
                return "Temp-banned";
            case BAN:
                return "Banned";
            case UNBAN:
                return "Unbanned";
            case UNMUTE:
                return "Unmuted";
        }
        return "N/A";
    }
}
