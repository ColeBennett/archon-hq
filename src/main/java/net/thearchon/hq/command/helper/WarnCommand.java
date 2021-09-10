package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;

public class WarnCommand extends Command {

    public WarnCommand(Archon archon) {
        super(archon, "/warn <player> <reason>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.error(getSyntax());
            return;
        }
        Player target = archon.matchOnlinePlayer(args[0]);
        if (target != null) {
            StringBuilder buf = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                buf.append(args[i]);
                buf.append(' ');
            }
            archon.getPunishManager().warn(player, target, buf.toString().trim());
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
