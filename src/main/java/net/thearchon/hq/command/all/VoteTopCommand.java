package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VoteTopCommand extends Command implements Runnable {

    private final List<String> msg = new ArrayList<>();

    public VoteTopCommand(Archon archon) {
        super(archon, "/votetop [month] [year]");

        archon.runTaskTimer(this, 1, TimeUnit.MINUTES);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.message(msg);
        } else if (args.length >= 1 && args.length <= 2) {
            String month = args[0].toLowerCase();
            try {
                Month.valueOf(month.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.error("Invalid month: " + month);
                return;
            }
            int year = 0;
            int now = Year.now().getValue();
            if (args.length == 2) {
                try {
                    year = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.error("Invalid year: " + args[1]);
                    return;
                }
                if (year < 2014 || year > now) {
                    player.error("Invalid year: " + year);
                    return;
                }
            } else {
                year = now;
            }
            player.message("&aLoading, please wait...");
            loadAndSend(player, month, year);
        } else {
            player.error(getSyntax());
        }
    }

    @Override
    public void run() {
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT MONTHNAME(time) AS month, (SELECT name FROM players WHERE id = player) AS name, COUNT(*) AS votes FROM votes WHERE" +
                            " YEAR(time) = YEAR(CURDATE()) && MONTH(time) = MONTH(CURDATE()) GROUP BY player ORDER BY votes DESC LIMIT 5;")) {
                msg.clear();
                int id = 1;
                while (rs.next()) {
                    if (msg.isEmpty()) {
                        msg.add("&6=== &a&lTop 5 Voters &6- &b&l" + rs.getString("month") + " &6===" );
                    }
                    String name = rs.getString("name");
                    if (name.equalsIgnoreCase("None")) continue;
                    msg.add("&7- &e#" + id++ + ": &3" + name + " &7(" + rs.getString("votes") + " Votes)");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
//        archon.getDb().executeQuery(rs -> {
//            try {
//                msg.clear();
//                int id = 1;
//                while (rs.next()) {
//                    if (msg.isEmpty()) {
//                        msg.add("&6=== &a&lTop 5 Voters &6- &b&l" + rs.getString("month") + " &6===" );
//                    }
//                    msg.add("&7- &e#" + id++ + ": &3" + rs.getString("name") + " &7(" + rs.getString("votes") + " Votes)");
//                }
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }, "SELECT MONTHNAME(time) AS month, (SELECT name FROM players WHERE id = player) AS name, COUNT(*) AS votes FROM votes WHERE" +
//                " YEAR(time) = YEAR(CURDATE()) && MONTH(time) = MONTH(CURDATE()) GROUP BY player ORDER BY votes DESC LIMIT 5;");
    }

    private void loadAndSend(Player player, String month, int year) {
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT MONTHNAME(time) AS month, YEAR(time) AS year, (SELECT name FROM players WHERE id = player) AS name, COUNT(*) AS votes FROM votes WHERE" +
                            " YEAR(time) = " + year + " && MONTHNAME(time) = '" + month + "' GROUP BY player ORDER BY votes DESC LIMIT 5;")) {
                List<String> msg = new ArrayList<>();
                int id = 1;
                while (rs.next()) {
                    if (msg.isEmpty()) {
                        msg.add("&6=== &a&lTop 5 Voters &6- &b&l" + rs.getString("month") + ' ' + rs.getString("year") + " &6===" );
                    }
                    msg.add("&7- &e#" + id++ + ": &3" + rs.getString("name") + " &7(" + rs.getString("votes") + " Votes)");
                }
                archon.runTask(() -> player.message(msg));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
//        archon.getDb().executeQuery(rs -> {
//            try {
//                List<String> msg = new ArrayList<>();
//                int id = 1;
//                while (rs.next()) {
//                    if (msg.isEmpty()) {
//                        msg.add("&6=== &a&lTop 5 Voters &6- &b&l" + rs.getString("month") + ' ' + rs.getString("year") + " &6===" );
//                    }
//                    msg.add("&7- &e#" + id++ + ": &3" + rs.getString("name") + " &7(" + rs.getString("votes") + " Votes)");
//                }
//                rs.close();
//                player.message(msg);
//            } catch (SQLException e) {
//                player.error("No votes found for " + month + ' ' + year + '.');
//                e.printStackTrace();
//            }
//        }, "SELECT MONTHNAME(time) AS month, YEAR(time) AS year, (SELECT name FROM players WHERE id = player) AS name, COUNT(*) AS votes FROM votes WHERE" +
//                " YEAR(time) = " + year + " && MONTHNAME(time) = '" + month + "' GROUP BY player ORDER BY votes DESC LIMIT 5;");
    }
}
