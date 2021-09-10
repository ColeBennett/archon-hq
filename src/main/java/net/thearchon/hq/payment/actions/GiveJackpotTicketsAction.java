package net.thearchon.hq.payment.actions;

import net.thearchon.hq.payment.PaymentAction;
import net.thearchon.hq.service.buycraft.PackageCommand;

public class GiveJackpotTicketsAction implements PaymentAction {

    @Override
    public void handle(PackageCommand command) {
//        int amount = 1;
//        try {
//            amount = Integer.parseInt(command.getArgs()[1]);
//        } catch (Exception ignored) {
//        }
//
//        String uuid = command.getUuid();
//        Player player = Archon.getInstance().getPlayerByUuid(uuid);
//        if (player != null) {
//            BukkitClient archon = player.getCurrentServer();
//            if (archon.getType() == ClientType.FACTIONS) {
//                archon.send(Protocol.JACKPOT_GIVE_TICKET.construct(uuid, amount));
//            }
//            player.message("&3** &a(Archon Shop) &6You have been credited &e" + Util.addCommas(amount) + " &6jackpot " + Util.pluralize("ticket", "s", amount) + "! &3**");
//        }
//        Archon.getInstance().getDb().execute("INSERT IGNORE INTO tickets VALUES (?, 0)", uuid);
//        Archon.getInstance().getDb().execute("UPDATE jp_tickets SET amount = amount + ? WHERE uuid = ?", amount, uuid);
    }
}
