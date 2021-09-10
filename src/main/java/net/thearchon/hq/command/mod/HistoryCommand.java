package net.thearchon.hq.command.mod;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HistoryCommand extends Command {

    public HistoryCommand(Archon archon) {
        super(archon, "/history <player> [keyword]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        String keyword = null;
        if (args.length == 2) {
            keyword = args[1];
        } else if (args.length > 2) {
            StringBuilder buf = new StringBuilder();
            int len = args.length;
            for (int i = 1; i < len; i++) {
                buf.append(args[i]);
                if (i != (len - 1)) {
                    buf.append(' ');
                }
            }
            keyword = buf.toString();
        }

        PlayerInfo pi = archon.getPlayerInfo(args[0]);
        if (pi == null) {
            pi = archon.getPlayerInfoAdv(args[0]);
        }
        if (pi == null) {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
            return;
        }
        PlayerInfo info = pi;

        player.message("&7Searching &6" + info.getName() + "'s &7command history"
                + (keyword != null ? " with keyword &a&o" + keyword + "&7" : "")
                + ", please wait...");

        String sql = "SELECT command, TIMESTAMPDIFF(SECOND, time, NOW()) AS secs FROM command_log WHERE id = " + info.getId();
        if (keyword != null) {
            sql += " && command LIKE '%" + keyword + "%'";
        }
        sql += " ORDER BY TIME DESC LIMIT 8";

        search(player, info, keyword, sql);
    }

    private void search(Player player, PlayerInfo info, String keyword, String sql) {
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                int results = 0;
                String[] msg = new String[10];
                String header = "&7-- &6[Results found] &7Name: &e" + info.getName();
                if (keyword != null) {
                    header += "&7, &a&o" + keyword;
                }
                header += " &7--";
                msg[0] = header;

                while (rs.next()) {
                    String cmd = rs.getString("command");
                    if (keyword != null) {
                        cmd = cmd.replace(keyword, "&b" + keyword + "&3");
                    }
                    msg[9 - results++] = "&3" + cmd + " &7&o(" + DateTimeUtil.formatTime(rs.getLong("secs"), true) + " ago)";
                }

                if (results == 0) {
                    player.error("No results found.");
                } else {
                    if (results < 8) {
                        msg[1] = "&a&o" + results + " record(s) found.";
                    } else {
                        msg[1] = "&a&oDisplaying latest " + results + " records.";
                    }

                    int idx = 0;
                    for (int i = msg.length - 1; i >= 2; i--) {
                        if (msg[i] == null) {
                            msg[i] = "&c&oNothing found here.";
                        }
                        msg[i] = "&7" + ++idx + ": " + msg[i];
                    }
                    player.message(msg);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
