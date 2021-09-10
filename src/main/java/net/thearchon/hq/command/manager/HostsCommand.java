package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

public class HostsCommand extends Command {

    public HostsCommand(Archon archon) {
        super(archon, "/hosts [hostname]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            String hostName = args[0].toLowerCase();
            if (!hostName.contains(".")) {
                hostName += ".thearchon.net";
            }
            List<String> users = new ArrayList<>();
            for (Player p : archon.getPlayers()) {
                String host = p.getHost();
                if (host != null && host.equals(hostName)) {
                    users.add(p.getName());
                }
            }
            Collections.sort(users);

            StringBuilder buf = new StringBuilder();
            Iterator<String> itr = users.iterator();
            while (itr.hasNext()) {
                buf.append("&7");
                buf.append(itr.next());
                if (itr.hasNext()) {
                    buf.append("&a, ");
                }
            }
            player.message("&cPlayers: " + buf.toString(),
                    "&c" + Util.addCommas(users.size())
                    + Util.pluralize(" player", "s", users.size())
                    + " using &6" + hostName + "&c.");
        } else {
            int sum = 0;
            Map<String, Integer> hosts = new HashMap<>();
            for (Player p : archon.getPlayers()) {
                String host = p.getHost();
                if (host != null) {
                    Integer count = hosts.get(host);
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }
                    hosts.put(host, count);
                    sum++;
                }
            }
            hosts = Util.sortByValue(hosts, true);
            List<String> msg = new ArrayList<>(hosts.size() + 1);
            msg.add("&cArchon Hosts with Player Counts:");
            for (Entry<String, Integer> entry : hosts.entrySet()) {
                int count = entry.getValue();
                double perc = sum != 0 ? ((double) count / (double) sum) * 100 : 0;
                msg.add(" &6" + entry.getKey() + ": &7"
                        + Util.addCommas(count)
                        + Util.pluralize(" player", "s", entry.getValue())
                        + " &a(" + decFormat.format(perc) + "%)");
            }
            player.message(msg);
        }
    }

    private final DecimalFormat decFormat = new DecimalFormat("0.#");
}
