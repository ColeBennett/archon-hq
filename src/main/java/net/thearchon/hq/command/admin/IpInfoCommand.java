package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

public class IpInfoCommand extends Command {

    public IpInfoCommand(Archon archon) {
        super(archon, "/ipinfo <player>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String input = args[0];
        if (input.equalsIgnoreCase("SimpleRansacking")) return;

        String sql = "SELECT (SELECT name FROM players WHERE players.id = ip_log.id) AS name, ip_address, date FROM ip_log WHERE id = (SELECT id FROM players WHERE LOWER(name) = '" + input.toLowerCase() + "' LIMIT 1) ORDER BY date DESC;";

        player.message("&aSearching, please wait...");

        archon.getDataSource().getConnection(conn -> {
            String foundName = input;
            Map<String, String> found = new LinkedHashMap<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    foundName = rs.getString("name");
                    found.put(rs.getString("ip_address"), rs.getString("date"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            final String name = foundName;
            archon.runTask(() -> {
                if (!found.isEmpty()) {
                    List<String> msg = new ArrayList<>(found.size() + 1);
                    String status = archon.hasPlayer(name) ? "Online" : "Offline";
                    String color = status.equals("Online") ? "&a" : "&4";
                    msg.add("&6Found &a" + found.size() + " &6ips used by &7" + name + color + " (" + status + ")");
                    int idx = 0;
                    for (Entry<String, String> entry : found.entrySet()) {
                        msg.add(" &7#" + ++idx + ": " + color + entry.getKey() + " &7- Last Used: &e" + entry.getValue());
                    }
                    player.message(msg);
                } else {
                    player.error("No players found matching ip or name: " + input);
                }
            });
        });
    }
}
