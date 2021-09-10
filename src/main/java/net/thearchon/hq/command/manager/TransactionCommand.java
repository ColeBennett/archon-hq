package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransactionCommand extends Command {

    public TransactionCommand(Archon archon) {
        super(archon, "/transaction <id>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String id = args[0].toUpperCase();
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT status, name, server, date, price, package FROM purchases WHERE UPPER(transaction) = '" + id + "'")) {
                if (rs.next()) {
                    List<String> list = new ArrayList<>();
                    for (String p : rs.getString("package").split("\\|")) {
                        if (!p.isEmpty()) {
                            list.add(p);
                        }
                    }
                    StringBuilder packages = new StringBuilder();
                    Iterator<String> itr = list.iterator();
                    while (itr.hasNext()) {
                        packages.append(itr.next());
                        if (itr.hasNext()) {
                            packages.append(", ");
                        }
                    }

                    String status = rs.getString("status");
                    if (status.equals("COMPLETE")) {
                        status = "&aComplete";
                    } else {
                        status = "&c" + status;
                    }

                    player.message(
                            "&7--- &6Transaction: &a" + id + " &7---",
                            "&3* &7Status: &f" + status,
                            "&3* &7User: &e" + rs.getString("name"),
                            "&3* &7Server: &e" + rs.getString("server"),
                            "&3* &7Date: &e" + rs.getString("date") + " (UTC)",
                            "&3* &7Price: &a$" + rs.getString("price"),
                            "&3* &7Package: &e" + packages.toString());
                } else {
                    player.error("No matches found for transaction id: " + id);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
