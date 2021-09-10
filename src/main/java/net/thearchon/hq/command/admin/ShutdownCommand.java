package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class ShutdownCommand extends Command {
    
    public ShutdownCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        player.message("&cShutting down ArchonHQ...");
        archon.shutdown();
    }
}
