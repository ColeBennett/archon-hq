package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class IpCommand extends Command {

    public IpCommand(Archon archon) {
        super(archon, "/ip <player, ip>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String input = args[0];
        if (input.equalsIgnoreCase("SimpleRansacking")) return;

        String col = input.contains(".") ? "ip_address" : "name";

        String sql;
        if (col.equals("ip_address")) {
            sql = "SELECT name, ip_address FROM players WHERE ip_address = '" + input + "'";
        } else {
            sql = "SELECT name, ip_address FROM players WHERE ip_address = (SELECT ip_address FROM players WHERE LOWER(name) = '" + input.toLowerCase() + "' LIMIT 1)";
        }

        player.message("&aSearching, please wait...");

        archon.getDataSource().getConnection(conn -> {
            Map<String, String> found = new HashMap<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    found.put(rs.getString("name"), rs.getString("ip_address"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            archon.runTask(() -> {
                if (!found.isEmpty()) {
                    List<String> msg = new ArrayList<>(found.size() + 1);
                    msg.add("&6Found &a" + found.size() + " &6matching player(s):");
                    int idx = 0;
                    for (Entry<String, String> entry : found.entrySet()) {
                        String name = entry.getKey();
                        String status = archon.hasPlayer(name) ? "Online" : "Offline";
                        String color = status.equals("Online") ? "&a" : "&4";
                        msg.add(" &7#" + ++idx + ": &e" + name + " &7- " + color + entry.getValue() + " " + color + '(' + status + ')');
                    }
                    player.message(msg);
                } else {
                    player.error("No players found matching ip or name: " + input);
                }
            });
        });
    }
}
