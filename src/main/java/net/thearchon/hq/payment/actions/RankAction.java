package net.thearchon.hq.payment.actions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.payment.PaymentAction;
import net.thearchon.hq.service.buycraft.PackageCommand;
import net.thearchon.nio.Protocol;

public class RankAction implements PaymentAction {

    @Override
    public void handle(PackageCommand command) {
        Rank rank = Rank.valueOf(command.getArgs()[1]);
        Archon.getInstance().getDataSource().execute("UPDATE players SET rank = ? WHERE uuid = ?;", rank.name(), command.getUuid());

        Archon.getInstance().getLogger().warning("PAYMENT " + command.getUuid() + "/" + command.getUuid() + ": " + command.getCommand());

        Player player = Archon.getInstance().getPlayerByUuid(command.getUuid());
        if (player != null) {
            player.setRank(rank);
            player.getCurrentServer().send(Protocol.RANK_UPDATE.construct(player.getId(), rank.name()));
            if (rank != Rank.DEFAULT) {
                player.message("&3** &a(Archon Shop) &6You are now rank " + rank.getPrefix() + " &6! &3**");
            }
        }

        if (rank != Rank.DEFAULT) {
            Archon.getInstance().sendAll(Protocol.BROADCAST.construct("&c&lArchon &8>> &6Thank you " + rank.getColor() + command.getUsername() + " &6for purchasing "
                    + rank.getPrefix() + " &6@ &cshop.thearchon.net&6!"), ServerType.ALL_LOBBIES);
        }
    }
}
