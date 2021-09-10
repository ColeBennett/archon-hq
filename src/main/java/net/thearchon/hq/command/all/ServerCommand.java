package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;

import java.util.*;

public class ServerCommand extends Command {
    
    public ServerCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<String> servers = new ArrayList<>(archon.getServerManager().getNames());
        Collections.sort(servers);
        if (args.length != 1) {
            servers.remove("setup");
            servers.remove("factiondev");
            Iterator<String> itr = servers.iterator();
            while (itr.hasNext()) { // TOOD TEMP
                String serv = itr.next();
                if (serv.contains("warfare")) {
                    itr.remove();
                }
            }
            displayServers(player, servers);
            return;
        }
        String serv = args[0].toLowerCase();
        if (servers.contains(serv)) {
            BukkitClient client = archon.getBukkitClient(serv);
            if (client != null) {
                if (client.getType().isMinigameType() && serv.matches(".*\\d+.*")) {
                    player.error("You cannot connect to " + serv + " by using this command.");
                    return;
                }
                if (client.getServerName().equals("setup") && !player.hasPermission(Rank.ADMIN)) {
                    player.error("You don't have permission to join this server.");
                    return;
                }
                archon.requestConnect(player, client);
            } else {
                player.error(Message.SERVER_OFFLINE.format(serv));
            }
        } else {
            player.error(Message.SERVER_NOT_FOUND.format(serv));
        }
    }
    
    private void displayServers(Player player, Collection<String> servers) {
        StringBuilder buf = new StringBuilder();
        int i = 0, size = servers.size();
        for (String server : servers) {
            buf.append("&7");
            buf.append(server);

            BukkitClient client = archon.getBukkitClientHolder(server);
            if (client != null) {
                if (client.getSlots() != 0) {
                    buf.append(" &3(&b");
                    buf.append(client.getOnlineCount());
                    buf.append("&3/&b");
                    buf.append(client.getSlots());
                    buf.append("&3)");
                } else {
                    buf.append(" &c(Offline)");
                }
            }

            if (i++ != (size - 1)) {
                buf.append("&a, ");
            }
        }
        player.message("&6Servers: " + buf.toString(), Message.PREFIX + "&6There are &a" + Util.addCommas(size) + " &6available servers.");
    }
}
