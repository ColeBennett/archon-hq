package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.command.Command;
import net.thearchon.nio.Protocol;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.Player;
import net.thearchon.hq.Settings;

public class MaintenanceCommand extends Command {

    public MaintenanceCommand(Archon archon) {
        super(archon);
    }
    
    @Override
    public void execute(Player player, String[] args) {
        Settings config = archon.getSettings();
        if (config.getMaintenanceMode()) {
            config.setMaintenanceMode(false);
            for (BungeeClient client : archon.getClients(BungeeClient.class)) {
                client.send(Protocol.MOTD_UPDATE.construct(config.getMotd(client.getRegion())));
            }
            player.message("&cDisabled &7maintenance mode.");
        } else {
            config.setMaintenanceMode(true);
            for (Player p : archon.getPlayers()) {
                if (!p.isStaff()) {
                    p.disconnect("&cWe are now currently under maintenance. Check back later!");
                }
            }
            archon.sendAll(Protocol.MOTD_UPDATE.construct(config.getMaintenanceMotd()), ServerType.BUNGEE);
            player.message("&aEnabled &7maintenance mode.");
        }
    }
}
