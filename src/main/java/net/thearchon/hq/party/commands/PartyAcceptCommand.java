package net.thearchon.hq.party.commands;

import net.thearchon.hq.party.Party;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class PartyAcceptCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        if (player.getParty() != null) {
            player.error("You're already in a party!");
            return;
        }
        if (args.length != 1) {
            player.error("/party accept <player>");
            return;
        }
        String name = args[0];
        Party party = null;
        for (Party p : player.getPartyInvites()) {
            if (p.getHost().getName().equalsIgnoreCase(name)) {
                party = p;
                break;
            }
        }
        if (party != null) {
            player.getPartyInvites().remove(party);
            player.setParty(party);
            player.setPartyChatEnabled(false);
            party.addMember(player);
            party.message(player.getDisplayName() + " &7joined the party!");
        } else {
            player.error("Invitation to " + name + "'s party not found!");
        }
    }
}
