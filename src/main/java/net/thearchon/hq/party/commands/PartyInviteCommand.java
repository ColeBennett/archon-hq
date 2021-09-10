package net.thearchon.hq.party.commands;

import net.thearchon.hq.Archon;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.party.Party;
import net.thearchon.hq.Player;

import java.util.concurrent.TimeUnit;

public class PartyInviteCommand extends Command {

    public PartyInviteCommand(Archon server) {
        super(server);
    }

    @Override
    public void execute(final Player player, String[] args) {
        final Party party = player.getParty();
        if (party == null) {
            player.error("You aren't in a party!");
            return;
        }
        if (args.length != 1) {
            player.error("/party invite <player>");
            return;
        }
        String input = args[0];
        final Player p = archon.matchOnlinePlayer(input);
        if (p == null) {
            player.error("Player not online: " + input);
            return;
        }
        if (p == player) {
            player.error("You cannot invite yourself to the party, you're the host!");
            return;
        }
        if (party.hasMember(p)) {
            player.error(p.getName() + " is already a member of your party!");
            return;
        }
        if (p.getPartyInvites().contains(party)) {
            player.error(p.getName() + " already has a pending invitation to your party!");
            return;
        }

        p.getPartyInvites().add(party);
        p.message("&eYou have been invited to " + player.getDisplayName() + "'s &eparty! &3Use &a/party join " + player.getName() + " &3to join!");
        p.message("&eInvitation expires in &c1 &eminute!");
        party.message(player.getDisplayName() + " &7invited " + p.getDisplayName() + " &7to the party!");

        archon.runTaskLater(() -> {
            if (p.getPartyInvites().remove(party)) {
                p.message("&eYour invitation to " + player.getDisplayName() + "'s &eparty has expired!");
            }
        }, 1, TimeUnit.MINUTES);
    }
}
