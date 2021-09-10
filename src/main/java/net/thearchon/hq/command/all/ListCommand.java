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

public class ListCommand extends Command {

    private final List<Rank> ranks = Arrays.asList(
            Rank.VIP, Rank.VIP_PLUS,
            Rank.MVP, Rank.MVP_PLUS,
            Rank.YOUTUBER,
            Rank.HELPER, Rank.HELPER_PLUS,
            Rank.TRIAL_MOD, Rank.JR_MOD, Rank.MOD, Rank.SR_MOD, Rank.HEAD_MOD,
            Rank.MANAGER);

    public ListCommand(Archon archon) {
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

//        if (player.getCurrentServer().getType().isLobbyType()) {
//            BukkitClient lobby = archon.getBukkitClientHolder("lobby1");
//            Map<BukkitClient, List<Player>> staff = new HashMap<>();
//            for (Player p : archon.getPlayers()) {
//                if (p.isStaff()) {
//                    BukkitClient server = p.getCurrentServer().getType() == ServerType.LOBBY
//                            ? lobby : p.getCurrentServer();
//                    List<Player> list = staff.get(server);
//                    if (list == null) {
//                        list = new ArrayList<>();
//                        staff.put(server, list);
//                    }
//                    list.add(p);
//                }
//            }
//
//            for (Entry<BukkitClient, List<Player>> entry : staff.entrySet()) {
//                List<Player> list = entry.getValue();
//                if (!list.isEmpty()) {
//                    StringBuilder buf = new StringBuilder();
//                    buf.append("&7");
//                    buf.append(entry.getKey().getDisplayName().replaceAll("\\d+", ""));
//                    buf.append(": ");
//                    if (staff.isEmpty()) {
//                        buf.append("&cNone");
//                    } else {
//                        int i = 0;
//                        for (Player p : list) {
//                            if (p.hasPermission(Rank.MANAGER)) continue;
//                            buf.append(p.getRank().getPrefix());
//                            buf.append(' ');
//                            buf.append(p.getRank().getColor());
//                            buf.append(p.getName());
//                            if (i++ != (list.size() - 1)) {
//                                buf.append("&7, ");
//                            }
//                        }
//                    }
//                    msg.add(buf.toString());
//                }
//            }
//
//        } else {
//            msg.add("&6Staff on &a" + player.getCurrentServer().getDisplayName() + "&6:");
//
//            List<Player> staff = new ArrayList<>();
//            for (Player p : player.getCurrentServer().getPlayers()) {
//                if (p.isStaff()) {
//                    staff.add(p);
//                }
//            }
//            StringBuilder buf = new StringBuilder();
////            buf.append(rank.getPrefix());
////            buf.append(" &7[&a").append(Util.addCommas(members.size()));
////            buf.append(" &aOnline&7] ");
//            if (staff.isEmpty()) {
//                buf.append("&7None");
//            } else {
//                int i = 0;
//                for (Player p : staff) {
//                    if (p.hasPermission(Rank.MANAGER)) continue;
//                    buf.append(p.getRank().getPrefix());
//                    buf.append(' ');
//                    buf.append(p.getRank().getColor());
//                    buf.append(p.getName());
//                    if (i++ != (staff.size() - 1)) {
//                        buf.append("&7, ");
//                    }
//                }
//            }
//            msg.add(" " + buf.toString());
//        }

        msg.add(Message.PREFIX + "&6There are &a" + archon.getOnlineCount() + "&7/&a" + archon.getSettings().getNetworkSlots() + " &6players online.");
        BukkitClient server = player.getCurrentServer();
        if (server != null && !server.getType().isLobbyType()) {
            msg.add(Message.PREFIX + "&6Playing on &e" + server.getDisplayName() + "&6. &a" + server.getOnlineCount() + "&7/&a" + server.getSlots() + " &6online.");
        }
        player.message(msg);
    }   
}
