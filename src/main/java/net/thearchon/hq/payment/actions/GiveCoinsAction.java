package net.thearchon.hq.payment.actions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.payment.PaymentAction;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.Protocol;
import net.thearchon.hq.service.buycraft.PackageCommand;

public class GiveCoinsAction implements PaymentAction {

    @Override
    public void handle(PackageCommand command) {
        int amount = 1;
        try {
            amount = Integer.parseInt(command.getArgs()[1]);
        } catch (NumberFormatException ignored) {
        }
        Player player = Archon.getInstance().getPlayerByUuid(command.getUuid());
        if (player != null) {
            int coins = player.getCoins() + amount;
            player.setCoins(coins);
            Archon.getInstance().getDataSource().execute("UPDATE players SET coins = ? WHERE id = ?", coins, player.getId());
            player.getCurrentServer().send(Protocol.COIN_UPDATE.construct(player.getId(), coins));
            player.message("&3** &a(Archon Shop) &6You have been credited &b" + Util.addCommas(amount) + " &6" + Util.pluralize("coin", "s", amount) + "! &3**");
        } else {
            Archon.getInstance().getDataSource().execute("UPDATE players SET coins = coins + ? WHERE uuid = ?", amount, command.getUuid());
        }
    }
}
