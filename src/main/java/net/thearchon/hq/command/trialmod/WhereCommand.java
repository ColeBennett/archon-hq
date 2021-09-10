package net.thearchon.hq.command.trialmod;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;

public class WhereCommand extends Command {
    
    public WhereCommand(Archon archon) {
        super(archon, "/where <player>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        Player target = archon.matchOnlinePlayer(args[0]);
        if (target != null) {
            BukkitClient server = target.getCurrentServer();
            if (server != null) {
                player.message(target.getDisplayName() + " &7is currently on: &a" + server.getServerName() + " &c(Proxy: " + target.getProxy().getId() + ")");
            } else {
                player.message(target.getDisplayName() + " &7is not on a server.");
            }
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
