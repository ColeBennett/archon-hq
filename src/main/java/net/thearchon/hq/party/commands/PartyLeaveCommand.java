package net.thearchon.hq.party.commands;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.party.Party;

public class PartyLeaveCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        Party party = player.getParty();
        if (party == null) {
            player.error("You aren't in a party!");
            return;
        }
        if (party.isHost(player)) {
            player.error("You cannot leave your party as the host. End your party by using /party end.");
            return;
        }
        party.removeMember(player);
        player.setParty(null);
        party.message(player.getDisplayName() + " &7left your party!");
        player.message("&eLeft " + party.getHost().getDisplayName() + "'s &eparty!");
    }
}
