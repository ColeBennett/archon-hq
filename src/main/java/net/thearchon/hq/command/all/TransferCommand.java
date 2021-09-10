package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.io.JsonConfig;
import net.thearchon.hq.util.io.JsonUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class TransferCommand extends Command {

    private final Yaml yaml = new Yaml();
    private final Map<String, File> files = new HashMap<>();
    private final Map<String, List<String>> ranks = new HashMap<>();
    private final Map<String, List<String>> transfers = new HashMap<>();
    private final Map<String, Map<String, Object>> cache = new HashMap<>();
    private final List<String> removed;

    public TransferCommand(Archon archon) {
        super(archon, "/transfer <from server> <to server>");

        File configFile = new File("transfers.json");
        JsonConfig config;
        boolean exists = false;
        if (!configFile.exists()) {
            config = new JsonConfig();
        } else {
            config = new JsonConfig(configFile);
            exists = true;
        }

//        for (String server : config.getKeys()) {
//            List<String> toServers = config.getStringList(server + ".transfers");
//            if (!toServers.isEmpty()) {
//                if (toServers.size() == 1) {
//                    String to = toServers.get(0);
//                    if (to.startsWith("allExcept")) {
//                        to = to.replace("allExcept", "").trim();
//                        transfers.put(server, allExcept(to));
//                    }
//                } else {
//                    transfers.put(server, toServers);
//                }
//            }
//            ranks.put(server, config.getStringList(server + ".ranks"));
//        }

        /**
         * Register archon ranks.
         */
        ranks.put("amber", Arrays.asList("endermite", "zombie", "blaze", "ghast", "wither", "enderdragon"));
        ranks.put("amethyst", Arrays.asList("farmer", "priest", "mage", "wizard", "demigod", "demon"));
        ranks.put("royal", Arrays.asList("corporal", "sergeant", "lieutenant", "captain", "major", "general"));
        ranks.put("ruby", Arrays.asList("opal", "topaz", "jade", "emerald", "pearl", "ruby"));
        ranks.put("cyanx", Arrays.asList("squid", "slime", "creeper", "witch", "guardian", "giant"));
        ranks.put("aqua", Arrays.asList("shrimp", "pufferfish", "walrus", "shark", "polarbear", "octopus"));
        ranks.put("chrome", Arrays.asList("worker", "attendant", "operative", "supervisor", "general"));
        ranks.put("fuschia", Arrays.asList("apprentice", "invoker", "warlock", "arcanist", "archmage", "arcanemagnus"));
        ranks.put("sapphire", Arrays.asList("jury", "suspect", "guard", "lawyer", "clerk", "judge"));
        ranks.put("silver", Arrays.asList("apollo", "ares", "hades", "hera", "poseidon", "zeus"));
        ranks.put("gold", Arrays.asList("officer", "agent", "detective", "deputy", "chief", "commander"));
        ranks.put("platinum", Arrays.asList("corporal", "sergeant", "lieutenant", "captain", "major", "general"));
        ranks.put("crimson", Arrays.asList("tenant", "baron", "earl", "count", "duke", "viceroy"));
        ranks.put("purple", Arrays.asList("bronze", "silver", "platinum", "diamond", "master", "challenger"));
        ranks.put("cyan", Arrays.asList("endermite", "zombie", "blaze", "ghast", "wither", "enderdragon"));

        List<String> redBlue = Arrays.asList("hero", "elite", "oracle", "god", "divine", "immortal");
        ranks.put("red", redBlue);
        ranks.put("blue", redBlue);
        ranks.put("monarch", redBlue);

        List<String> greenOrange = Arrays.asList("coal", "iron", "gold", "diamond", "obsidian", "bedrock");
        ranks.put("green", greenOrange);
        ranks.put("orange", greenOrange);

        List<String> whiteBlack = Arrays.asList("citizen", "knight", "prince", "pope", "emperor", "king");
        ranks.put("white", whiteBlack);
        ranks.put("black", whiteBlack);
        ranks.put("oblivion", whiteBlack);

        /*
         * Register transfer scenarios.
         */
        removed = Arrays.asList(
                "platinum", "cyan", "orange", "red", "crimson",
                "fuschia", "purple", "white", "chrome", "gold", "green",
                "ruby", "amethyst", "amber", "royal");
        transfers.put("ruby", allExcept(Collections.singletonList("phantom")));
        transfers.put("platinum", allExcept(Collections.singletonList("phantom")));
        transfers.put("cyan", allExcept(Collections.singletonList("phantom")));
        transfers.put("orange", allExcept(Collections.singletonList("phantom")));
        transfers.put("purple", allExcept(Collections.singletonList("phantom")));
        transfers.put("red", allExcept(Collections.singletonList("phantom")));
        transfers.put("crimson", allExcept(Collections.singletonList("phantom")));
        transfers.put("fuschia", allExcept(Collections.singletonList("phantom")));
        transfers.put("white", allExcept(Collections.singletonList("phantom")));
        transfers.put("chrome", allExcept(Collections.singletonList("phantom")));
        transfers.put("gold", allExcept(Collections.singletonList("phantom")));
        transfers.put("cyanx", allExcept(Collections.singletonList("phantom")));
        transfers.put("green", allExcept(Collections.singletonList("phantom")));
        transfers.put("amber", allExcept(Collections.singletonList("phantom")));
        transfers.put("royal", allExcept(Collections.singletonList("phantom")));
        transfers.put("amethyst", allExcept(Collections.singletonList("phantom")));
        transfers.put("sapphire", allExcept(Collections.singletonList("phantom")));
        transfers.put("blue", allExcept(Collections.singletonList("phantom")));
        transfers.put("black", allExcept(Collections.singletonList("phantom")));

        if (!exists) {
            for (Entry<String, List<String>> entry : transfers.entrySet()) {
                config.set(entry.getKey() + ".ranks", ranks.get(entry.getKey()));
                config.set(entry.getKey() + ".transfer", entry.getValue());
            }
            config.save(configFile);
        }

        /**
         * Load yml permission files.
         */
        File dir = new File("data/transfer");
        dir.mkdir();
        for (String s : transfers.keySet()) {
            File file = new File(dir, s + ".yml");
            if (file.exists()) {
                files.put(s, file);
                cache.put(s, loadYml(file));
            }
        }
    }

    private List<String> allExcept(String toExclude) {
        List<String> servers = new ArrayList<>();
        for (String server : ranks.keySet()) {
            if (removed.contains(server) || server.equals(toExclude)) continue;
            servers.add(server);
        }
        return servers;
    }

    private List<String> allExcept(List<String> toExclude) {
        List<String> servers = new ArrayList<>();
        for (String server : ranks.keySet()) {
            if (removed.contains(server) || toExclude.contains(server)) continue;
            servers.add(server);
        }
        return servers;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (files.isEmpty()) {
            player.error("Rank transfers are currently disabled.");
            return;
        }
        if (args.length > 2) {
            player.error(getSyntax());
            return;
        }

        String uuid = player.getUuid();
        Map<String, Set<String>> data = searchAllRanks(uuid);
        if (args.length < 2) {
            if (data.isEmpty()) {
                player.error("You have no available ranks to transfer.");
            } else {
                List<String> msg = new ArrayList<>();
                msg.add("&7&m------------------------------------------------");
                msg.add("&c&lArchon &8>> &6Available ranks you can transfer:");
                List<String> avail = new ArrayList<>();
                for (String s : archon.getServerManager().getNames()) {
                    if (s.contains("dev")) continue;
                    if (s.startsWith("faction")) {
                        avail.add(s.replace("faction", ""));
                    }
                }
                for (Entry<String, Set<String>> entry : data.entrySet()) {
                    List<String> to = transfers.get(entry.getKey());
                    Iterator<String> itr = to.iterator();
                    while (itr.hasNext()) {
                        if (!avail.contains(itr.next())) {
                            itr.remove();
                        }
                    }
                    msg.add("&7- &a" + entry.getKey() + " &7to &3" + formatList(to));
                }
                msg.add("");
                msg.add("&6Use &a/transfer <from server> <to server> &6to transfer.");
                msg.add("&4Note: &c&oThis action is irreversible. Also transfers extra abilies. (Special Kits, Jelly Legs, etc.)");
                msg.add("&7&m----------------------------------------");
                player.message(msg);
            }
            return;
        }

        String from = args[0].toLowerCase();
        if (!ranks.containsKey(from)) {
            player.error("Faction server not found: " + from);
            return;
        }
        if (!transfers.containsKey(from)) {
            player.error(format(from) + " does not allow rank transfers. Allowed: &6" + formatSet(transfers.keySet()));
            return;
        }

        String to = args[1].toLowerCase();
        if (!ranks.containsKey(to)) {
            player.error("Faction server not found: " + to);
            return;
        }
        if (to.equals(from)) {
            player.error("You cannot transfer ranks to the same server.");
            return;
        }

        File file = files.get(from);
        if (!file.exists()) {
            player.error(format(from) + " rank transfers are currently disabled.");
            return;
        }

        Set<String> playerRanks = data.get(from);
        if (playerRanks == null) {
            player.error("You don't have a rank on " + format(from) + " to transfer.");
            return;
        }

        List<String> acceptedServers = transfers.get(from);
        if (!acceptedServers.contains(to)) {
            player.error("You cannot transfer " + format(from) + " ranks to " + format(to)
                    + ". Accepted servers: &6" + formatList(acceptedServers));
            return;
        }

        /**
         * Find equivalent ranks.
         */
        List<String> fromRanks = ranks.get(from);
        List<String> toRanks = ranks.get(to);

        List<String> transferRanksFrom = new ArrayList<>();
        List<String> transferRanksTo = new ArrayList<>();
        Set<String> transferred = new HashSet<>();

        List<String> msg = new ArrayList<>();
        msg.add("&7&m------------------------------------------------");
        msg.add("&6Ranks Transferred:");
        for (String rank : playerRanks) {
            int idx = indexOf(fromRanks, rank);
            if (idx > toRanks.size() - 1) {
                idx--; // In case it is transferring a $600 rank to a archon that doesn't have a $600 rank.
            }

            String equiv = format(toRanks.get(idx));
            transferred.add(equiv);
            transferRanksFrom.add(rank);
            transferRanksTo.add(equiv.toLowerCase());
            msg.add(" &7- &c" + format(rank) + " (faction" + from + ") &7-> &a" + equiv + " (faction" + to + ')');
        }
        msg.add("");

        /**
         * Find existing permissions.
         */
        msg.add("&6Permissions/Abilities Transferred:");
        Set<String> perms = searchPermissions(from, uuid);
        if (perms != null) {
            for (String perm : perms) {
                String disp = perm;
                if (disp.contains("nofalldamage")) {
                    disp = "Jelly Legs";
                } else if (disp.contains("silkspawners.silkdrop")) {
                    disp = "Mine Spawners (Silk Touch)";
                } else if (disp.contains("ffly")) {
                    disp = "Fly Ability";
                } else if (disp.contains("tntfill")) {
                    disp = "TNT Fill Ability";
                } else if (disp.contains("tntcraft")) {
                    disp = "TNT Craft";
                } else if (disp.contains("ffly.fly")) {
                    disp = "Fly";
                }
                msg.add(" &7- &a" + disp);
            }
        } else {
            msg.add("&7None");
        }
        msg.add("");

        /**
         * Execute transfer on faction server.
         */
        BukkitClient serv = archon.getBukkitClient("faction" + to);
        if (!serv.isActive()) {
            player.error(serv.getServerName() + " is currently offline. Please try again later.");
            return;
        }

        for (String rank : transferred) {
            serv.runConsoleCommand("perm player " + player.getName() + " addgroup " + rank);
        }
        if (perms != null) {
            for (String perm : perms) {
                serv.runConsoleCommand("perm player " + player.getName() + " set " + perm);
            }
            removePermissions(from, uuid);
        }
        removeRanks(from, uuid);
        save(from);

        msg.add("&dTransfer complete! &7(Factions " + format(from) + " -> Factions " + format(to) + ")");
        msg.add("&7&m------------------------------------------------");
        player.message(msg);

        archon.getDataSource().execute("INSERT INTO transfer_log VALUES(?, NOW(), ?, ?, ?, ?);",
                player.getId(), from, to, JsonUtil.toJsonCompact(transferRanksFrom), JsonUtil.toJsonCompact(transferRanksTo));
        archon.notifyStaff("&3[TRANSFER] " + player.getDisplayName() + " &7transferred &6" + format(from) + " &7-> &6" + format(to));
    }

    private Map<String, Set<String>> searchAllRanks(String uuid) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Entry<String, File> entry : files.entrySet()) {
            String server = entry.getKey();
            Set<String> ranks = searchRanks(server, entry.getValue(), uuid);
            if (ranks != null) {
                result.put(server, ranks);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<String> searchRanks(String server, File file, String uuid) {
        if (!file.exists()) return null;
        uuid = uuid.replace("-", "");
        List<String> serverRanks = ranks.get(server);

        Set<String> result = null;
        Map<String, Object> data = getData(server);

        List<Map<String, Object>> groups = (List<Map<String, Object>>) data.get("groups");
        for (Map<String, Object> group : groups) {
            String groupName = ((String) group.get("name")).toLowerCase();
            if (!serverRanks.contains(groupName)) continue;
            List<Map<String, String>> entries = (List<Map<String, String>>) group.get("members");
            for (Map<String, String> entry : entries) {
                String foundUuid = entry.get("uuid");
                if (foundUuid != null && foundUuid.equals(uuid)) {
                    if (result == null) {
                        result = new HashSet<>();
                    }
                    result.add(groupName);
                    break;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void removeRanks(String server, String uuid) {
        File file = files.get(server);
        if (!file.exists()) return;
        uuid = uuid.replace("-", "");

        List<String> serverRanks = ranks.get(server);
        Map<String, Object> data = getData(server);

        List<Map<String, Object>> groups = (List<Map<String, Object>>) data.get("groups");
        for (Map<String, Object> group : groups) {
            String groupName = ((String) group.get("name")).toLowerCase();
            if (!serverRanks.contains(groupName)) continue;
            List<Map<String, String>> entries = (List<Map<String, String>>) group.get("members");
            Iterator<Map<String, String>> itr = entries.iterator();
            while (itr.hasNext()) {
                Map<String, String> entry = itr.next();
                String foundUuid = entry.get("uuid");
                if (foundUuid != null && foundUuid.equals(uuid)) {
                    itr.remove();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> searchPermissions(String server, String uuid) {
        File file = files.get(server);
        if (!file.exists()) return null;
        uuid = uuid.replace("-", "");

        Set<String> result = null;
        Map<String, Object> data = getData(server);

        List<Map<String, Object>> players = (List<Map<String, Object>>) data.get("players");
        for (Map<String, Object> pdata : players) {
            String foundUuid = ((String) pdata.get("uuid"));
            if (foundUuid.equals(uuid)) {
                Map<String, Boolean> perms = (Map<String, Boolean>) pdata.get("permissions");
                for (Entry<String, Boolean> entry : perms.entrySet()) {
                    if (entry.getValue()) {
                        if (result == null) {
                            result = new HashSet<>();
                        }
                        result.add(entry.getKey());
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void removePermissions(String server, String uuid) {
        File file = files.get(server);
        if (!file.exists()) return;
        uuid = uuid.replace("-", "");

        Map<String, Object> data = getData(server);

        List<Map<String, Object>> players = (List<Map<String, Object>>) data.get("players");
        Iterator<Map<String, Object>> itr = players.iterator();
        while (itr.hasNext()) {
            String foundUuid = ((String) itr.next().get("uuid"));
            if (foundUuid.equals(uuid)) {
                itr.remove();
            }
        }
    }

    private void save(String server) {
        File file = files.get(server);
        if (file.exists() && cache.containsKey(server)) {
            try {
                yaml.dump(cache.get(server), new FileWriter(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Object> getData(String server) {
        return cache.get(server);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYml(File file) {
        Map<String, Object> data = null;
        try {
            data = (Map<String, Object>) yaml.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private int indexOf(List<String> list, String elem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(elem)) {
                return i;
            }
        }
        return -1;
    }

    private String formatSet(Set<String> set) {
        StringBuilder buf = new StringBuilder();
        Iterator<String> itr = set.iterator();
        while (itr.hasNext()) {
            buf.append(itr.next());
            if (itr.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    private String formatList(List<String> list) {
        Collections.sort(list);
        StringBuilder buf = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buf.append(list.get(i));
            if (size > 1 && i != size - 1) {
                if (i == size - 2) {
                    buf.append(", or ");
                } else {
                    buf.append(", ");
                }
            }
        }
        return buf.toString();
    }

    private String format(String str) {
        if (str.length() < 2) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
