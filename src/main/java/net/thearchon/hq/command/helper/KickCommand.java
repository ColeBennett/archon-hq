package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class KickCommand extends Command {

    public KickCommand(Archon archon) {
        super(archon, "/kick <player>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        Player target = archon.matchOnlinePlayer(args[0]);
        if (target != null) {
            StringBuilder buf = new StringBuilder();
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    buf.append(args[i]);
                    buf.append(' ');
                }
            }
            archon.getPunishManager().kick(player, target, buf.toString().trim());
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
