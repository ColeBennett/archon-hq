package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.Protocol;
import net.thearchon.hq.PlayerInfo;

public class GiveCoinsCommand extends Command {

    public GiveCoinsCommand(Archon archon) {
        super(archon, "/givecoins <player> <amount>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.error(getSyntax());
            return;
        }
        String name = args[0];
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.error("Invalid number: " + args[1]);
            return;
        }
        Player p = archon.getPlayerByName(name);
        if (p != null) {
            p.setCoins(p.getCoins() + amount);
            archon.getDataSource().execute("UPDATE players SET coins = ? WHERE id = ?", p.getCoins(), p.getId());
            p.getCurrentServer().send(Protocol.COIN_UPDATE.construct(p.getId(), p.getCoins()));
            player.message("&aGave &b" + Util.addCommas(amount) + " coin(s) to &7" + p.getName() + "&a.");
        } else {
            PlayerInfo info = archon.getPlayerInfo(name);
            if (info == null) {
                player.error("Player not found in database. (" + name + ")");
                return;
            }
            archon.getDataSource().execute("UPDATE players SET coins = coins + ? WHERE id = ?", amount, info.getId());
            player.message("&aGave &b" + Util.addCommas(amount) + " &acoin(s) to &7" + info.getName() + "&a.");
        }
    }
}
