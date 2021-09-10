package net.thearchon.hq.command.mod;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.PlayerInfo;

public class UnbanCommand extends Command {

    public UnbanCommand(Archon archon) {
        super(archon, "/unban <player|uuid>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
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
                archon.getPunishManager().unban(info, player);
            } else {
                player.error(info.getName() + " hasn't been banned.");
            }
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
