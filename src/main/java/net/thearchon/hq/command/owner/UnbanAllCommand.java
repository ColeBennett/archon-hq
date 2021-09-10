package net.thearchon.hq.command.owner;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class UnbanAllCommand extends Command {

    public UnbanAllCommand(Archon archon) {
        super(archon, "/unbanall <secret>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.error("/unbanall <secret>");
            return;
        }
        if (args[0].equals("cobaeisbae")) {
            archon.getPunishManager().unbanAll(player);
        } else {
            player.error("Incorrect secret key: " + args[0]);
        }
    }
}
