package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SeenCommand extends Command {

    public SeenCommand(Archon archon) {
        super(archon, "/seen <player>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }

        String input = args[0];
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name, TIMESTAMPDIFF(SECOND, seen, NOW()) AS seconds FROM players WHERE name = '" + input + "'")) {
                String name = null;
                Long seconds = null;
                if (rs.next()) {
                    name = rs.getString("name");
                    seconds = rs.getLong("seconds");
                }
                if (name == null) {
                    player.error(Message.PLAYER_NOT_FOUND.format(input));
                    return;
                }

                String status = archon.hasPlayer(name) ? "Online" : "Offline";
                String color = status.equals("Online") ? "&a" : "&4";
                player.message("&e" + name + " " + color + status + " &7- " + color + DateTimeUtil.formatTime(seconds));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
