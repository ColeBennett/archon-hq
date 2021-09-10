package net.thearchon.hq.command.mod;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.Player;
import net.thearchon.hq.PlayerInfo;

public class BanCommand extends Command {

    public BanCommand(Archon archon) {
        super(archon, "/ban <player|uuid> [reason]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        String input = args[0];
        PlayerInfo info;
        if (input.contains("-")) {
            info = archon.getPlayerInfoByUuid(input);
        } else {
            info = archon.getPlayerInfo(input);
        }
        if (info != null) {
            if (archon.getPunishManager().hasRecord(info)) {
                player.error(info.getName() + " has already been banned.");
            } else {
                StringBuilder buf = new StringBuilder();
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        buf.append(args[i]);
                        buf.append(' ');
                    }
                }
                archon.getPunishManager().ban(info, buf.toString().trim(), player);
            }
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
