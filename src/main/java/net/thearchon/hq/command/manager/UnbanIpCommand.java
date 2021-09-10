package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class UnbanIpCommand extends Command {

    public UnbanIpCommand(Archon archon) {
        super(archon, "/unbanip <ip>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }
        String ip = args[0];
        if (!ip.contains(".")) {
            player.error("Please enter a valid ip address.");
            return;
        }
        if (archon.getPunishManager().hasIpRecord(ip)) {
            archon.getPunishManager().unbanIp(ip, player);
        } else {
            player.error("IP " + ip + " is not currently banned.");
        }
    }
}
