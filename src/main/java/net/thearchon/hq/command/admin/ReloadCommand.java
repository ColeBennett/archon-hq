package net.thearchon.hq.command.admin;

import net.thearchon.hq.*;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;

public class ReloadCommand extends Command {
    
    public ReloadCommand(Archon archon) {
        super(archon);
    }
    
    @Override
    public void execute(Player player, String[] args) {
        long start = System.currentTimeMillis();

        archon.getServerManager().load();
        player.message("&6Reloaded &3&o" + ServerManager.FILE + " &7(" + (System.currentTimeMillis() - start) + " ms)");

        start = System.currentTimeMillis();
        archon.reloadSettings();
        player.message("&6Reloaded &3&o" + Settings.FILE + " &7(" + (System.currentTimeMillis() - start) + " ms)");

        start = System.currentTimeMillis();
        archon.getAutoAlerts().load();
        player.message("&6Reloaded &3&o" + AutoAlerts.FILE + " &7(" + (System.currentTimeMillis() - start) + " ms)");

        player.message(Message.PREFIX + "&aReloaded all ArchonHQ configuration files.");
    }
}
