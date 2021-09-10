package net.thearchon.hq.party.commands;

import net.thearchon.hq.party.Party;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.command.Command;

public class PartyHostCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission(Rank.VIP)) {
            player.error("You must have VIP or higher to host a party! Visit our shop @ &6http://shop.thearchon.net");
            return;
        }
        if (player.getParty() != null) {
            player.error("You are already hosting a party!");
            return;
        }
        Party party = new Party();
        party.setHost(player);
        player.setParty(party);
        player.message("&3You are now hosting your own party! Invite players by using &6/party invite <player>");
    }
}
