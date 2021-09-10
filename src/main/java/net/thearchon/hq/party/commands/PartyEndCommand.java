package net.thearchon.hq.party.commands;

import net.thearchon.hq.party.Party;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class PartyEndCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        Party party = player.getParty();
        if (party == null) {
            player.error("You aren't in a party!");
            return;
        }
        if (!party.isHost(player)) {
            player.error("You must be the host of a party to end it!");
            return;
        }
        party.message(player.getDisplayName() + " &chas ended the party!");
        player.setParty(null);
        for (Player member : party.getMembers()) {
            member.setParty(null);
        }
    }
}
