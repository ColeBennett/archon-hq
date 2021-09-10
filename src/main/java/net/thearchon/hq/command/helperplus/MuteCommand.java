package net.thearchon.hq.command.helperplus;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.PlayerInfo;

import java.util.Arrays;

public class MuteCommand extends Command {
    
    public MuteCommand(Archon archon) {
        super(archon, "/mute <player|uuid> <amount> <s:sec, m:min, h:hour, d:day> [reason]");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 3) {
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
            if (archon.getPunishManager().hasMuteRecord(info)) {
                player.error(info.getName() + " has already been muted.");
                return;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.error(getSyntax());
                return;
            }
            String unit = args[2].toLowerCase();
            if (!Arrays.asList("s", "m", "h", "d").contains(unit)) {
                player.error(getSyntax());
                return;
            }
            if (amount < 1) {
                player.error("Invalid amount: " + amount);
                return;
            }
            if (unit.equals("s") && amount > 60) {
                player.error("Use minutes for time when muting over 60 seconds.");
                return;
            } else if (unit.equals("m") && amount > 60) {
                player.error("Use hours for time when muting over 60 minutes.");
                return;
            } else if (unit.equals("h") && amount > 24) {
                player.error("Use days for time when muting over 24 hours.");
                return;
            } else if (unit.equals("d")) {
                if (!player.hasPermission(Rank.JR_MOD) && amount > 7) {
                    player.error("You cannot mute players for longer than 7 days.");
                    return;
                }
                if (amount > 365) {
                    player.error("You cannot mute players for longer than 365 days.");
                    return;
                }
            }
            String reason = "";
            if (args.length > 3) {
                StringBuilder buf = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    buf.append(args[i]);
                    buf.append(' ');
                }
                reason = buf.toString().trim();
            }
            long converted = 0;
            switch (unit) {
                case "s":
                    converted = 1000 * amount;
                    break;
                case "m":
                    converted = 60000 * amount;
                    break;
                case "h":
                    converted = (60000 * 60) * amount;
                    break;
                case "d":
                    converted = ((60000 * 60) * 24) * amount;
                    break;
            }
            archon.getPunishManager().mute(info, System.currentTimeMillis() + converted, reason, amount + unit, player, converted);
        } else {
            player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
        }
    }
}
