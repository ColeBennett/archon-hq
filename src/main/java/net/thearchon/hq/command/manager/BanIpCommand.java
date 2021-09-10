package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BanIpCommand extends Command {

    public BanIpCommand(Archon archon) {
        super(archon, "/banip <ip|player> [reason]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        String input = args[0];
        String query = null;
        if (input.contains(".")) {
            if (archon.getPunishManager().hasIpRecord(input)) {
                player.error("IP " + input + " is already banned.");
                return;
            }
            query = "SELECT ip_address FROM players WHERE ip_address = '" + input + "';";
        } else {
            PlayerInfo info = archon.getPlayerInfo(input);
            if (info == null) {
                player.error(Message.PLAYER_NOT_FOUND.format(input));
                return;
            }
            query = "SELECT ip_address FROM players WHERE id = " + info.getId() + ";";
        }

        boolean exists = false;
        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             if (rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (exists) {
            String reason = null;
            if (args.length > 1) {
                StringBuilder buf = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    buf.append(args[i]);
                    if (i != args.length - 1) {
                        buf.append(' ');
                    }
                }
                reason = buf.toString();
            }
            archon.getPunishManager().banIp(input, reason, player);
        } else {
            player.error("No players found matching ip: " + input);
        }
    }
}
