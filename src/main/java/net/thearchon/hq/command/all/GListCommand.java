package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GListCommand extends Command {

    private final List<Rank> ranks = Arrays.asList(
            Rank.VIP, Rank.VIP_PLUS,
            Rank.MVP, Rank.MVP_PLUS,
            Rank.YOUTUBER,
            Rank.HELPER, Rank.HELPER_PLUS,
            Rank.TRIAL_MOD, Rank.JR_MOD, Rank.MOD, Rank.SR_MOD, Rank.HEAD_MOD);

    public GListCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<String> msg = new ArrayList<>();

        for (Rank rank : ranks) {
            List<String> members = new ArrayList<>();
            for (Player p : archon.getPlayers()) {
                if (p.getRank() == rank) {
                    members.add(p.getName());
                }
            }
            StringBuilder buf = new StringBuilder();
            buf.append(rank.getPrefix());
            buf.append(" &7[&a").append(Util.addCommas(members.size()));
            buf.append(" &aOnline&7] ");
            if (members.isEmpty()) {
                buf.append("&cNone");
            } else {
                int i = 0;
                for (String s : members) {
                    buf.append("&7");
                    buf.append(s);
                    if (i++ != (members.size() - 1)) {
                        buf.append("&a, ");
                    }
                }
            }
            msg.add(buf.toString());
        }

        msg.add(Message.PREFIX + "&6There are &a" + archon.getOnlineCount() + "&7/&a" + archon.getSettings().getNetworkSlots() + " &6players online.");
        BukkitClient server = player.getCurrentServer();
        if (server != null && !server.getType().isLobbyType()) {
            msg.add(Message.PREFIX + "&6Playing on &e" + server.getDisplayName() + "&6. &a" + server.getOnlineCount() + "&7/&a" + server.getSlots() + " &6online.");
        }
        player.message(msg);
    }   
}
