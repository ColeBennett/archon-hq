package net.thearchon.hq.party.commands;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.party.Party;

import java.util.Iterator;

public class PartyInfoCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        Party party = player.getParty();
        if (party == null) {
            player.error("You aren't in a party!");
            return;
        }
        StringBuilder buf = new StringBuilder();
        Iterator<Player> itr = party.getMembers().iterator();
        while (itr.hasNext()) {
            buf.append(itr.next().getDisplayName());
            if (itr.hasNext()) {
                buf.append("&7, ");
            }
        }
        player.message(
                "&7--- " + party.getHost().getDisplayName() + "'s Party &7---",
                "&7Max Players: &e" + party.getMaxPlayers(),
                "&7Members (" + party.getMembers().size() + "): " + buf.toString()
        );
    }
}
