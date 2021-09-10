package net.thearchon.hq.party.commands;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class PartyChatCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        if (player.getParty() == null) {
            player.error("You aren't in a party!");
            return;
        }
        if (player.getPartyChatEnabled()) {
            player.setPartyChatEnabled(false);
            player.message("&cDisabled party chat mode!");
        } else {
            player.setPartyChatEnabled(true);
            player.message("&aEnabled party chat mode!");
        }
    }
}
