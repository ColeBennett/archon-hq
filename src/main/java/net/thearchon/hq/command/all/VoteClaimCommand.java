package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;

public class VoteClaimCommand extends Command {

    public VoteClaimCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        int votes = player.getUnclaimedVotes();
        if (votes > 0) {
            if (player.getCurrentServer().getType() != ServerType.FACTIONS) {
                player.error("You must be on a faction server to claim your reward!");
                return;
            }

            if (player.getCurrentServer().getServerName().equalsIgnoreCase(archon.getSettings().getString("disabledClaimServer"))) {
                player.error("Claim is currently disabled for this server.");
                return;
            }

            votes--;
            archon.getDataSource().execute("UPDATE players SET unclaimed_votes = ? WHERE id = ?;", votes, player.getId());
            player.setUnclaimedVotes(votes);
            if (player.getCurrentServer() != null) {
                if (archon.getSettings().getBoolean("doubleVoteRewards")) {
                    player.getCurrentServer().runConsoleCommand("arsys reward " + player.getName());
                }
                player.getCurrentServer().runConsoleCommand("arsys reward " + player.getName());
            }
            player.message("&6You just claimed your reward on &a" + player.getCurrentServer().getServerName() + "&6!");
            player.message("&6Unclaimed votes: " + (votes == 0 ? "&cNone" : "&b" + Util.addCommas(votes)));
        } else {
            player.error("You do not have any votes to claim. Use /vote to get the links!");
        }
    }
}
