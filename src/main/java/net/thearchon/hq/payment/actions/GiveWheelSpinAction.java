package net.thearchon.hq.payment.actions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.payment.PaymentAction;
import net.thearchon.hq.service.buycraft.PackageCommand;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.Protocol;

public class GiveWheelSpinAction implements PaymentAction {

    @Override
    public void handle(PackageCommand command) {
        int amount = 1;
        try {
            amount = Integer.parseInt(command.getArgs()[1]);
        } catch (NumberFormatException ignored) {
        }
        Player player = Archon.getInstance().getPlayerByUuid(command.getUuid());
        if (player != null) {
            BukkitClient server = player.getCurrentServer();
            if (server.getType() == ServerType.ARCADE_LOBBY) {
                server.send(Protocol.GIVE_WHEEL_SPINS.construct(player.getId(), amount));
            }
            player.message("&3** &a(Archon Shop) &6You have been credited &b" + Util.addCommas(amount) + " &6" + Util.pluralize("wheel spin", "s", amount) + "! &3**");
        }
        Archon.getInstance().getDataSource().execute("UPDATE arcade SET wheel_spins = wheel_spins + ? WHERE id = (SELECT id FROM players WHERE uuid = ?)", amount, command.getUuid());
    }
}
