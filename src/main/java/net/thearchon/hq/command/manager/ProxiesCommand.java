package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

public class ProxiesCommand extends Command {

    public ProxiesCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        int total = 0;
        Map<Integer, Integer> proxies = new TreeMap<>();
        for (Player p : archon.getPlayers()) {
            BungeeClient proxy = p.getProxy();
            if (proxy != null) {
                int id = proxy.getId();
                Integer count = proxies.get(id);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                proxies.put(id, count);
                total++;
            }
        }

        List<String> msg = new ArrayList<>(proxies.size() + 1);
        msg.add("&cArchon Proxies with Player Counts:");
        for (Entry<Integer, Integer> entry : proxies.entrySet()) {
            int count = entry.getValue();
            double perc = total != 0 ? ((double) count / (double) total) * 100 : 0;
            msg.add(" &6Bungee " + entry.getKey() + ": &7"
                    + Util.addCommas(count)
                    + Util.pluralize(" player", "s", entry.getValue())
                    + " &a(" + decFormat.format(perc) + "%)");
        }
        player.message(msg);
    }

    private final DecimalFormat decFormat = new DecimalFormat("0.#");
}
