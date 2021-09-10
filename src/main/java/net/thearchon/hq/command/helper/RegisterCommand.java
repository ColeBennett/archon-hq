package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegisterCommand extends Command {

    public RegisterCommand(Archon archon) {
        super(archon, "/register <password>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String password = args[0];
        if (!password.matches("^[a-zA-Z0-9_]*$")) {
            player.error("Password can only contain letters, numbers, and underscores.");
            return;
        }
        if (password.length() < 6 || password.length() > 24) {               
            player.error("Password length must be between 6 and 24 characters.");
            return;
        }
        String curr = null;
        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT password FROM archonhq WHERE id = " + player.getId())) {
             if (rs.next()) {
                curr = rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (curr == null) {
            archon.getDataSource().execute("INSERT INTO archonhq (id, password) VALUES (?, ?)", player.getId(), password);
            message(player, "&7Successfully registered with password &a&o'" + password + "'&7.");
        } else {
            if (curr.equalsIgnoreCase(password)) {
                player.error("You have already registered with this password.");
                return;
            }
            archon.getDataSource().execute("UPDATE archonhq SET password = ? WHERE id = ?", password, player.getId());
            message(player, "&7Replaced old password &c&o'" + curr + "' &7with &a&o'" + password + "'&7.");
        }
        player.message("&3Use &b/appinfo &3to learn how to access the application.");
    }
    
    static void message(Player player, String message) {
        player.message("&6&o[AHQ] " + message);
    }
}
