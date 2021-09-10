package net.thearchon.hq.party.commands;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.party.Party;

public class PartyKickCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        Party party = player.getParty();
        if (party == null) {
            player.error("You aren't in a party!");
            return;
        }
        if (!party.isHost(player)) {
            player.error("You must be the host of the party to kick players!");
            return;
        }
        if (args.length != 1) {
            player.error("/party kick <player>");
            return;
        }
        String input = args[0];
        Player p = archon.matchOnlinePlayer(input);
        if (p == null) {
            player.error("Player not online: " + input);
            return;
        }
        if (p == player) {
            player.error("You cannot kick yourself from the party, you're the host!");
            return;
        }
        if (!party.hasMember(p)) {
            player.error(p.getName() + " isn't a member of your party!");
            return;
        }
        party.removeMember(p);
        p.setParty(null);
        p.message("&eYou have been kicked from the party!");
        party.message(player.getDisplayName() + " &7kicked " + p.getDisplayName() + " &7from the party!");
    }
}
