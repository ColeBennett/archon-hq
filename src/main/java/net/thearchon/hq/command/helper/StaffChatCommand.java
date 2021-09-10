package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.nio.Protocol;

public class StaffChatCommand extends Command {

    public StaffChatCommand(Archon archon) {
        super(archon, "/sc [silent]");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (player.getRank().name().startsWith("HCF_")) {
            return;
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("silent")) {
                if (player.hasStaffSilent()) {
                    player.setStaffSilent(false);
                    player.message("&cDisabled &7staff &6&lSILENT &7mode.");
                } else {
                    player.setStaffSilent(true);
                    player.message("&aEnabled &7staff &6&lSILENT &7mode.");
                }
            } else {
                player.error(getSyntax());
            }
            return;
        }
        if (player.hasStaffChatEnabled()) {
            player.setStaffChatEnabled(false);
            player.getProxy().send(Protocol.ENABLE_CHAT.construct(player.getUuid()));
            player.message("&cDisabled &7staff chat.");
        } else {
            player.setStaffChatEnabled(true);
            player.getProxy().send(Protocol.DISABLE_CHAT.construct(player.getUuid()));
            player.message("&aEnabled &7staff chat.");
        }
    }  
}
