package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.punish.Punishment;
import net.thearchon.hq.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffHistoryCommand extends Command {

    public StaffHistoryCommand(Archon archon) {
        super(archon, "/staffhistory <staff> [filter: kick, tempban, ban, unban]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.error(getSyntax());
            return;
        }

        String input = args[0];

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
                sql = "SELECT (SELECT name FROM players WHERE players.id = punished) AS punished_name, type, DATE(time) AS date, duration, reason FROM punishment_log WHERE punisher = ?" +
                        " && type = '" + filter.name() + "' ORDER BY date DESC LIMIT 50;";
            } else {
                sql = "SELECT (SELECT name FROM players WHERE players.id = punished) AS punished_name, type, DATE(time) AS date, duration, reason FROM punishment_log WHERE punisher = ?" +
                        " ORDER BY date DESC LIMIT 50;";
            }

            player.message(Message.SEARCHING);
            archon.getDataSource().getConnection(conn -> {
                List<String> msg = new ArrayList<>();
                msg.add("&7&m---&r &6" + info.getName() + "'s &aStaff Record &7&m---&r");
                int count = 0;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql.replace("?", "" + info.getId()))) {
                    while (rs.next()) {
                        Punishment type = Punishment.valueOf(rs.getString("type"));
                        String punished = rs.getString("punished_name");
                        String line = "&7" + displayName(type) + " &c" + punished;
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
