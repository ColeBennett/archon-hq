package net.thearchon.hq;

import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.client.LobbyClient;
import net.thearchon.hq.handler.BungeeHandler;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.handler.ServerJoinQueue;
import net.thearchon.hq.handler.event.EventsClient;
import net.thearchon.hq.handler.event.EventsHandler;
import net.thearchon.hq.handler.factions.FactionsClient;
import net.thearchon.hq.handler.factions.FactionHandler;
import net.thearchon.hq.handler.prison.PrisonClient;
import net.thearchon.hq.handler.prison.PrisonHandler;
import net.thearchon.hq.handler.rankup.RankupClient;
import net.thearchon.hq.handler.rankup.RankupHandler;
import net.thearchon.hq.handler.setup.SetupClient;
import net.thearchon.hq.handler.warfare.WarfareLobbyClient;
import net.thearchon.hq.handler.warfare.WarfareLobbyHandler;
import net.thearchon.hq.util.io.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class ServerManager {

    public static final File FILE = new File("servers.json");

    private final Archon archon;
    private final Set<Client> clients = new LinkedHashSet<>();
    private final Set<String> names = new LinkedHashSet<>();

    public ServerManager(Archon archon) {
        this.archon = archon;

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
                JsonUtil.save(FILE, new HashMap<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        archon.getLogger().info("Loading servers...");
        long start = System.currentTimeMillis();

        Map<String, Object> json = JsonUtil.load(FILE, JsonUtil.MAP_STR_TYPE);

        clients.clear();
        names.clear();

        SetupClient setup = new SetupClient(archon);
        setup.setRegion(ServerRegion.NA);
        setup.setIpAddress("");
        setup.setDatacenter("");
        add(setup);

        for (Entry<String, Object> section : json.entrySet()) {
            ServerType type = ServerType.valueOf(section.getKey());

            /**
             * Bungee
             */
            if (type == ServerType.BUNGEE) {
                BungeeHandler handler = archon.getHandler(type);

                Map<String, Map<String, String>> entries = (Map<String, Map<String, String>>) section.getValue();
                for (Entry<String, Map<String, String>> entry : entries.entrySet()) {
                    Map<String, String> values = entry.getValue();

                    int id = Integer.parseInt(entry.getKey());
                    BungeeClient client = handler.getClient(id);
                    if (client == null) {
                        client = new BungeeClient(archon, id, values.get("datacenter"));
                        handler.addClient(client);
                    }
                    setAttributes(client, values);
                    archon.getLogger().info("Bungee " + id + " - " + client.toString());
                }
            }

            /**
             * Lobby
             */
            else if (type == ServerType.LOBBY) {
                LobbyHandler handler = archon.getHandler(type);

                Map<String, Map<String, String>> boxes = (Map<String, Map<String, String>>) section.getValue();
                for (Entry<String, Map<String, String>> box : boxes.entrySet()) {
                    Map<String, String> values = box.getValue();

                    String ip = values.get("ip");
                    String location = values.get("location");
                    ServerRegion region = ServerRegion.valueOf(values.get("region"));
                    String datacenter = box.getKey();

                    int idmin = Integer.parseInt(values.get("idmin"));
                    int idmax = Integer.parseInt(values.get("idmax"));

                    for (int i = idmin; i <= idmax; i++) {
                        LobbyClient client = handler.getClient(i);
                        if (client == null) {
                            client = new LobbyClient(archon, i);
                            handler.addClient(client);
                        }
                        client.setIpAddress(ip);
                        client.setPort(5000 + i);
                        client.setLocation(location);
                        client.setRegion(region);
                        client.setDatacenter(datacenter);

                        add(client);
                        archon.getLogger().info("Lobby " + i + " - " + client.toString());
                    }
                }
            }

            /**
             * Factions
             */
            else if (type == ServerType.FACTIONS) {
                FactionHandler handler = archon.getHandler(type);

                Map<String, Map<String, Object>> entries = (Map<String, Map<String, Object>>) section.getValue();
                for (Entry<String, Map<String, Object>> entry : entries.entrySet()) {
                    Map<String, Object> values = entry.getValue();

                    String serverName = entry.getKey();
                    ChatColor color = ChatColor.valueOf((String) values.get("color"));
                    String youtuber = (String) values.get("youtuber");

                    Map<Integer, String> maps = new HashMap<>();
                    if (values.containsKey("maps")) {
                        for (Entry<String, String> mapEntry : ((Map<String, String>) values.get("maps")).entrySet()) {
                            maps.put(Integer.parseInt(mapEntry.getKey()), mapEntry.getValue());
                        }
                    }

                    FactionsClient client = handler.getClient(serverName);
                    if (client == null) {
                        client = new FactionsClient(archon, serverName, color, youtuber, maps);
                        handler.addClient(client);
                    } else {
                        client.setColor(color);
                        client.setYoutuber(youtuber);
                        client.setMaps(maps);
                    }
                    setAttributesObj(client, values);

                    if (client.getServerName().equals("factionhardcore")) {
                        if (client.getQueue() == null) {
                            client.setQueue(new ServerJoinQueue(client));
                        }
                    }

                    add(client);
                    archon.getLogger().info("Factions (" + serverName + ") - " + client.getColor().name() + " - " + client.toString());
                }
            }

            /**
             * Prison
             */
            else if (type == ServerType.PRISON) {
                PrisonHandler handler = archon.getHandler(type);

                Map<String, Map<String, String>> entries = (Map<String, Map<String, String>>) section.getValue();
                for (Entry<String, Map<String, String>> entry : entries.entrySet()) {
                    String serverName = entry.getKey();
                    PrisonClient client = handler.getClient(serverName);
                    if (client == null) {
                        client = new PrisonClient(archon, serverName);
                        handler.addClient(client);
                    }
                    setAttributes(client, entry.getValue());

                    add(client);
                    archon.getLogger().info("Prison (" + client.getServerName() + ") - " + client.toString());
                }
            }

            /**
             * Rankup
             */
            else if (type == ServerType.RANKUP) {
                RankupHandler handler = archon.getHandler(type);

                RankupClient client = handler.getClient("rankup");
                if (client == null) {
                    client = new RankupClient(archon);
                    handler.addClient(client);
                }
                setAttributes(client, (Map<String, String>) section.getValue());

                add(client);
                archon.getLogger().info("Rankup (" + client.getServerName() + ") - " + client.toString());
            }

            /**
             * Events
             */
            else if (type == ServerType.EVENTS) {
                EventsHandler handler = archon.getHandler(type);

                EventsClient client = handler.getClient("events");
                if (client == null) {
                    client = new EventsClient(archon);
                    handler.addClient(client);
                }
                setAttributes(client, (Map<String, String>) section.getValue());

                add(client);
                archon.getLogger().info("Events (" + client.getServerName() + ") - " + client.toString());
            }

            /**
             * Warfare
             */
            else if (type == ServerType.WARFARE) {
                WarfareLobbyHandler handler = archon.getHandler(ServerType.WARFARE_LOBBY);

                Map<String, Object> s = (Map<String, Object>) section.getValue();

                Map<String, Map<String, String>> lobbyBoxes = (Map<String, Map<String, String>>) s.get("LOBBY");
                for (Entry<String, Map<String, String>> box : lobbyBoxes.entrySet()) {
                    Map<String, String> values = box.getValue();
                    int idmin = Integer.parseInt(values.get("idmin"));
                    int idmax = Integer.parseInt(values.get("idmax"));

                    int port = 6001;
                    for (int i = idmin; i <= idmax; i++) {
                        WarfareLobbyClient client = handler.getClient(i);
                        if (client == null) {
                            client = new WarfareLobbyClient(archon, i);
                        }
                        setAttributes(client, values);
                        client.setPort(port++);
                        handler.addClient(client);
                        add(client);
                    }
                }
            }
        }
        archon.getLogger().info("Loaded servers: " + clients.size() + " (Took " + (System.currentTimeMillis() - start) + " ms)");
    }

    public Set<Client> getClients() {
        return clients;
    }

    public Set<String> getNames() {
        return names;
    }

    public int getCount() {
        return clients.size();
    }

    private void add(BukkitClient client) {
        clients.add(client);
        names.add(client.getServerName());
    }

    private void setAttributesObj(Client client, Map<String, Object> values) {
        Map<String, String> m = new HashMap<>();
        for (Entry<String, Object> entry : values.entrySet()) {
            m.put(entry.getKey(), entry.getValue().toString());
        }
        setAttributes(client, m);
    }

    private void setAttributes(Client client, Map<String, String> values) {
        String ip = values.get("ip");
        int port = 25565;
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            ip = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                archon.getLogger().warning("Invalid port in IP address: " + parts[1]);
            }
        }
        client.setIpAddress(ip);
        client.setPort(port);
        client.setLocation(values.get("location"));
        try {
            client.setRegion(ServerRegion.valueOf(values.get("region")));
        } catch (IllegalArgumentException e) {
            client.setRegion(ServerRegion.NA);
        }
        client.setDatacenter(values.get("datacenter"));
    }
}
