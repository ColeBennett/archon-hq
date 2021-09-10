package net.thearchon.hq.command.jrmod;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountsCommand extends Command {

    public AccountsCommand(Archon archon) {
        super(archon, "/accounts <player>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String input = args[0];
        if (input.equalsIgnoreCase("SimpleRansacking")) return;

        String name = null, ip = null;
        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, ip_address FROM players WHERE name = '" + input + "'")) {
            if (rs.next()) {
                name = rs.getString("name");
                ip = rs.getString("ip_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (name == null) {
            player.error(Message.PLAYER_NOT_FOUND.format(input));
            return;
        }
        search(player, input, ip, name);
    }

    private void search(Player player, String input, String ip, String name) {
        player.message("&aSearching, please wait...");
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM players WHERE ip_address = '" + ip + "' && name != '" + name + "'")) {
                Set<String> found = new HashSet<>();
                while (rs.next()) {
                    String n = rs.getString("name");
                    if (n.equalsIgnoreCase("SimpleRansacking")) continue;
                    found.add(n);
                }
                if (!found.isEmpty()) {
                    List<String> msg = new ArrayList<>(found.size() + 1);
                    msg.add("&6Found &a" + found.size() + " &6related accounts(s):");
                    int idx = 0;
                    for (String n : found) {
                        String status = archon.hasPlayer(n) ? "Online" : "Offline";
                        String color = status.equals("Online") ? "&a" : "&4";
                        msg.add(" &7#" + ++idx + ": &e" + n + " " + color + '(' + status + ')');
                    }
                    player.message(msg);
                } else {
                    player.error("No players found matching user's ip: " + input);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
