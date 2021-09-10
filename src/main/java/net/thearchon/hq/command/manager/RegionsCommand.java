package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerRegion;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class RegionsCommand extends Command {

    public RegionsCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        int total = 0;
        Map<ServerRegion, Integer> regions = new TreeMap<>();
        for (Player p : archon.getPlayers()) {
            BungeeClient proxy = p.getProxy();
            if (proxy != null) {
                ServerRegion region = proxy.getRegion();
                Integer count = regions.get(region);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                regions.put(region, count);
                total++;
            }
        }

        List<String> msg = new ArrayList<>(regions.size() + 1);
        msg.add("&cArchon Regions with Player Counts:");
        for (Entry<ServerRegion, Integer> entry : regions.entrySet()) {
            int count = entry.getValue();
            double perc = total != 0 ? ((double) count / (double) total) * 100 : 0;
            msg.add(" &6Bungee " + entry.getKey().getName() + "/" + entry.getKey().getIp() + ": &7"
                    + Util.addCommas(count)
                    + Util.pluralize(" player", "s", entry.getValue())
                    + " &a(" + decFormat.format(perc) + "%)");
        }
        player.message(msg);
    }

    private final DecimalFormat decFormat = new DecimalFormat("0.#");
}
