package net.thearchon.hq;

import io.netty.channel.ChannelId;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import jline.console.ConsoleReader;
import net.thearchon.hq.DataSource.AsyncResult;
import net.thearchon.hq.app.AppHandler;
import net.thearchon.hq.app.HttpServ;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.command.CommandManager;
import net.thearchon.hq.data.DataRepository;
import net.thearchon.hq.data.LocalCache;
import net.thearchon.hq.event.EventManager;
import net.thearchon.hq.handler.*;
import net.thearchon.hq.handler.event.EventsHandler;
import net.thearchon.hq.handler.factions.FactionHandler;
import net.thearchon.hq.handler.factions.FactionsClient;
import net.thearchon.hq.handler.prison.PrisonHandler;
import net.thearchon.hq.handler.rankup.RankupHandler;
import net.thearchon.hq.handler.setup.SetupHandler;
import net.thearchon.hq.handler.uhc.UhcHostedHandler;
import net.thearchon.hq.handler.warfare.WarfareHandler;
import net.thearchon.hq.handler.warfare.WarfareLobbyHandler;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.metrics.ServerCountLog;
import net.thearchon.hq.payment.PaymentLog;
import net.thearchon.hq.punish.PunishManager;
import net.thearchon.hq.security.HostBlocker;
import net.thearchon.hq.service.ServiceManager;
import net.thearchon.hq.task.TaskManager;
import net.thearchon.hq.template.TemplateManager;
import net.thearchon.hq.util.Pair;
import net.thearchon.hq.util.DateTimeUtil;
import net.thearchon.hq.util.io.JsonUtil;
import net.thearchon.hq.util.logger.ConsoleLogger;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.Packet;
import net.thearchon.nio.protocol.impl.RequestPacket;
import net.thearchon.nio.server.ClientListener;
import net.thearchon.nio.server.DataServer;
import net.thearchon.nio.server.NioDataServer;
import net.thearchon.nio.server.ServerFutureListener;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Archon {

    private static Archon instance;

    /**
     * Main function of program.
     */
    public static void main(String[] args) {
        int port = 9090;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.exit(0);
            }
        }
        new Archon(port);
    }

    public static Archon getInstance() {
        return instance;
    }

    private final DataServer dataServer;
    private final NetHandler netHandler;

    private DataSource dataSource;
    private PaymentLog paymentLog;

    /**
     * Managers
     */
    private final ServerManager serverManager;
    private final CommandManager commandManager;
    private final ServiceManager serviceManager;
    private final PunishManager punishManager;
    private final TemplateManager templateManager;
    private final EventManager eventManager;

    private final HostBlocker hostBlocker;
    private final AutoAlerts autoAlerts;

    // Not used
    private ServerCountLog metricManager;
    private final Logger logger;

    private ConsoleReader consoleReader;
    private LocalCache cache;

    private Settings settings;
    private final File updatesFolder = new File("updates");
    private final DataRepository dataRepository = new DataRepository();

    private boolean dailytopLast;
    private long inboundPacketCount, outboundPacketCount;
    private final AtomicInteger ppsCounter = new AtomicInteger();
    private final AtomicInteger packetsPerSecond = new AtomicInteger();

    private final Map<Integer, Player> players = new HashMap<>(5000);
    private final Map<ChannelId, Client> clients = new HashMap<>();
    private final Map<ServerType, Handler> handlers = new HashMap<>();
    private final Map<String, Pair<Client, RequestPacket>> failedLogins = new HashMap<>();

    public Archon(int port) {
        instance = this;

        try {
            consoleReader = new ConsoleReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = new ConsoleLogger(this, consoleReader);
        logger.info("Loading ArchonHQ...");

        if (!LocalCache.FILE.exists()) {
            cache = new LocalCache();
            cache.save();
        } else {
            cache = JsonUtil.load(LocalCache.FILE, LocalCache.class);
        }

        updatesFolder.mkdir();
        initSettings();

        dataServer = new NioDataServer(new ServerFutureListener() {
            @Override
            public void bindSucceeded() {
                logger.info("Server now running: " + dataServer.getAddress());
            }

            @Override
            public void bindFailed() {
                logger.severe("Failed to start archon (port bind failure)");
                System.exit(0);
            }
        });
        dataServer.addHandler(netHandler = new NetHandler(this));

        dataSource = new DataSource(this);
        dataSource.initMysql();
        dataSource.initMongoDb();

        loadHandlers();

        // Keep order
        eventManager = new EventManager(this);
        punishManager = new PunishManager(this);
        serverManager = new ServerManager(this);
        metricManager = new ServerCountLog(this);
        commandManager = new CommandManager(this);
        templateManager = new TemplateManager(this);
        paymentLog = new PaymentLog(this);
        serviceManager = new ServiceManager(this);
        new TaskManager(this);

        hostBlocker = new HostBlocker(this);
        autoAlerts = new AutoAlerts(this);

        reloadSettings();

        logger.info("Starting server...");
        dataServer.bind(port);

        runTaskTimer(() -> sendAll(getServerCountList(), ServerType.ALL_LOBBIES), 5, 1, TimeUnit.SECONDS);

        dataServer.getWorkerGroup().scheduleAtFixedRate(() ->
                packetsPerSecond.set(ppsCounter.getAndSet(0)), 0, 1, TimeUnit.SECONDS);

        runTaskTimer(() -> { // TODO TEMP
            Set<BukkitClient> clients = getBukkitClientHolders();
            for (BukkitClient cl : clients) {
                for (Player p : cl.getPlayers()) {
                    if (!players.containsValue(p)) {
                        getLogger().severe("[TASK] Player found on " + cl.getServerName() + " but not in players list: " + p.getName() + "/" + p.getUuid());
                    }
                }
            }
            int broken = 0;
            for (Player p : players.values()) {
                if (p.getCurrentServer() == null) {
                    broken++;
                    getLogger().severe("[TASK] Player does not have a server: " + p.getName() + "/" + p.getUuid() + " - seen: " + DateTimeUtil.formatTime((System.currentTimeMillis() - p.getSessionStart()) / 1000));
                } else {
                    if (!p.getCurrentServer().hasPlayer(p)) {
                        getLogger().severe("[TASK] Player is not on assigned server: " + p.getCurrentServer().getServerName() + ", " + p.getName() + "/" + p.getUuid());
                    }
                }
            }
            if (broken > 0) {
                getLogger().severe("[TASK] " + broken + " online out of " + players.size() + " without a server");
            }
        }, 1, 1, TimeUnit.MINUTES);

        new Thread(() -> {
            new HttpServ().start();
        }).start();

        serviceManager.initServices();
        initConsole();
    }

    private void loadHandlers() {
//        SwHandler swHandler = new SwHandler(this);
//        handlers.put(ServerType.SKYWARS, swHandler);
//        handlers.put(ServerType.SKYWARS_LOBBY, new SwLobbyHandler(this, swHandler));
//
//        SgHandler sgHandler = new SgHandler(this);
//        handlers.put(ServerType.SG, sgHandler);
//        handlers.put(ServerType.SG_LOBBY, new SgLobbyHandler(this, sgHandler));
//
//        SgbHandler sgbHandler = new SgbHandler(this);
//        handlers.put(ServerType.SGB, sgbHandler);
//        handlers.put(ServerType.SGB_LOBBY, new SgbLobbyHandler(this, sgbHandler));
//
//        ArcadeHandler arcadeHandler = new ArcadeHandler(this);
//        handlers.put(ServerType.ARCADE, arcadeHandler);
//        handlers.put(ServerType.ARCADE_LOBBY, new ArcadeLobbyHandler(this, arcadeHandler));

//        handlers.put(ServerType.UHC, new UhcHandler(this));

        WarfareHandler warfareHandler = new WarfareHandler(this);
        handlers.put(ServerType.WARFARE, warfareHandler);
        handlers.put(ServerType.WARFARE_LOBBY, new WarfareLobbyHandler(this, warfareHandler));

        handlers.put(ServerType.UHC_HOSTED, new UhcHostedHandler(this));
        handlers.put(ServerType.RANKUP, new RankupHandler(this));
        handlers.put(ServerType.EVENTS, new EventsHandler(this));

        handlers.put(ServerType.PRISON, new PrisonHandler(this));
        handlers.put(ServerType.FACTIONS, new FactionHandler(this));
        handlers.put(ServerType.SETUP, new SetupHandler(this));
        handlers.put(ServerType.LOBBY, new LobbyHandler(this));

        handlers.put(ServerType.BUNGEE, new BungeeHandler(this));
        handlers.put(ServerType.APP, new AppHandler(this));
    }

    public boolean addPlayer(Player player) {
        return players.put(player.getId(), player) == null;
    }

    public boolean removePlayer(Player player) {
        return players.remove(player.getId()) != null;
    }

    public void reloadSettings() {
        if (settings != null) {
            for (String disabled : settings.getDisabledCommands()) {
                commandManager.unregister(disabled);
            }
        }

        initSettings();

        for (String disabled : settings.getDisabledCommands()) {
            commandManager.register(disabled, new Command() {
                @Override
                public void execute(Player player, String[] args) {
                    player.error(Message.COMMAND_DISABLED);
                }
            }, Rank.DEFAULT);
        }

        ((BungeeHandler) getHandler(ServerType.BUNGEE)).updateServerLists();
        sendAll(Protocol.NETWORK_SLOT_UPDATE.construct(settings.getNetworkSlots()), ServerType.BUNGEE, ServerType.LOBBY, ServerType.APP);
        sendAll(Protocol.MOTD_UPDATE.construct(settings.getMaintenanceMode() ? settings.getMaintenanceMotd() : settings.getMotd()), ServerType.APP);
        for (BungeeClient client : getClients(BungeeClient.class)) {
            client.send(Protocol.MOTD_UPDATE.construct(settings.getMotd(client.getRegion())));
        }
    }

    private void initSettings() {
        settings = new Settings();
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends Handler> T getHandler(ServerType type) {
        return (T) handlers.get(type);
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends Client> Collection<T> getAllClients(ServerType handlerType) {
        Handler<T> h = getHandler(handlerType);
        return h.getClients();
    }

    public void respond(Client client, RequestPacket request, Packet packet) {
        ClientListener channel = client.getChannel();
        if (channel != null) {
            respond(channel, request, packet);
            client.addPacketSent();
        }
    }

    public void respond(ClientListener channel, RequestPacket request, Packet packet) {
        if (channel.isActive()) {
            channel.respond(request, packet);
            outboundPacketCount++;
            ppsCounter.getAndIncrement();
            logger.info("resp [" + packet.getClass().getSimpleName().toLowerCase() + "] to " + channel.getId());
        }
    }

    public void send(Client client, Packet packet) {
        ClientListener channel = client.getChannel();
        if (channel != null) {
            send(channel, packet);
            client.addPacketSent();
        }
    }

    public void send(ClientListener channel, Packet packet) {
        if (!channel.isActive()) return;
        channel.send(packet);
        outboundPacketCount++;
        ppsCounter.getAndIncrement();
        if (packet instanceof BufferedPacket) {
            Protocol header = Protocol.valueOf(((BufferedPacket) packet).getShort(0));
            if (header != Protocol.SERVER_COUNT_LIST) {
                logger.info("sent [" + header.name().toLowerCase() + "] to " + channel.getId());
            }
        } else {
            logger.info("sent [" + packet.getClass().getSimpleName().toLowerCase() + "] to " + channel.getId());
        }
    }

    public void sendAll(Packet packet) {
        for (Client client : clients.values()) {
            send(client, packet);
        }
    }

    public int sendAll(Packet packet, ServerType type) {
        int sent = 0;
        for (Client client : clients.values()) {
            if (client.getType() == type) {
                send(client, packet);
                sent++;
            }
        }
        return sent;
    }

    public int sendAll(Packet packet, ServerType... types) {
        int sent = 0;
        List<ServerType> list = Arrays.asList(types);
        for (Client client : clients.values()) {
            if (list.contains(client.getType())) {
                send(client, packet);
                sent++;
            }
        }
        return sent;
    }

    public BukkitClient getBukkitClient(String serverName) {
        for (Client client : clients.values()) {
            if (client instanceof BukkitClient) {
                BukkitClient bc = (BukkitClient) client;
                if (bc.getServerName().equalsIgnoreCase(serverName)) {
                    return bc;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public BukkitClient getBukkitClientHolder(String serverName) {
        for (Handler handler : handlers.values()) {
            if (handler instanceof BukkitHandler) {
                BukkitHandler<?> h = (BukkitHandler) handler;
                if (h.getClients() == null) continue;
                for (BukkitClient bc : h.getClients()) {
                    if (bc.getServerName().equalsIgnoreCase(serverName)) {
                        return bc;
                    }
                }
            }
        }
        return null;
    }

    public Set<BukkitClient> getBukkitClientHolders() {
        Set<BukkitClient> found = new HashSet<>();
        for (Handler handler : handlers.values()) {
            if (handler instanceof BukkitHandler) {
                BukkitHandler<?> h = (BukkitHandler) handler;
                if (h.getClients() != null) {
                    found.addAll(h.getClients());
                }
            }
        }
        return found;
    }

    public int getClientCount(ServerType type) {
        int count = 0;
        for (Client client : clients.values()) {
            if (client.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public <T extends Client> int getClientCount(Class<T> clazz) {
        int count = 0;
        for (Client client : clients.values()) {
            if (clazz.isAssignableFrom(client.getClass())) {
                count++;
            }
        }
        return count;
    }

    public Set<Client> getClients(ServerType type) {
        return clients.values().stream()
                .filter(client -> client.getType() == type)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getClients(Class<T> clazz) {
        return clients.values().stream()
                .filter(client -> clazz.isAssignableFrom(client.getClass()))
                .map(client -> (T) client).collect(Collectors.toSet());
    }

    public Set<Client> getClientsExcluding(ServerType type) {
        return clients.values().stream()
                .filter(client -> client.getType() != type)
                .collect(Collectors.toSet());
    }

    public Set<Client> getClientsExcluding(ServerType... types) {
        List<ServerType> list = Arrays.asList(types);
        return clients.values().stream()
                .filter(client -> !list.contains(client.getType()))
                .collect(Collectors.toSet());
    }

    public void alert(String message) {
        sendAll(Protocol.BROADCAST.construct(message), ServerType.BUNGEE);
    }

    public void alert(String... messages) {
        sendAll(Protocol.BROADCAST.construct((Object[]) messages), ServerType.BUNGEE);
    }

    public void notifyStaff(String message) {
        notifyStaff(message, true);
    }

    public void notifyStaff(String message, boolean withPrefix) {
        if (withPrefix) {
            message = Message.STAFF_PREFIX + message;
        }
        for (Player player : players.values()) {
            if (player.isStaff() && !player.hasStaffSilent()) {
                player.message(message);
            }
        }
    }

    public void notifyStaff(String... messages) {
        players.values().stream().filter(player -> player.isStaff() && !player.hasStaffSilent())
                .forEach(player -> player.message(messages));
    }

    public void onStaffChatMessage(String name, Rank rank, String message) {
        notifyStaff(rank.getPrefix() + " &7" + name + " &8> &f" + message);
    }

    public Player getPlayer(int id) {
        for (Player player : players.values()) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public Player getPlayerByName(String name) {
        for (Player player : players.values()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    public Player getPlayerByUuid(String uuid) {
        for (Player player : players.values()) {
            if (player.getUuid().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public Player matchOnlinePlayer(String input) {
        input = input.toLowerCase();
        for (Player player : players.values()) {
            if (player.getName().toLowerCase().startsWith(input)) {
                return player;
            }
        }
        return null;
    }

    public boolean hasPlayer(String name) {
        return getPlayerByName(name) != null;
    }

    public boolean hasPlayer(int id) {
        return players.containsKey(id);
    }

    public Map<ChannelId, Client> getActiveClients() {
        return clients;
    }

    public PunishManager getPunishManager() {
        return punishManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public HostBlocker getHostBlocker() {
        return hostBlocker;
    }

    public AutoAlerts getAutoAlerts() {
        return autoAlerts;
    }

    public Settings getSettings() {
        return settings;
    }

    public LocalCache getCache() {
        return cache;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public PaymentLog getPaymentLog() {
        return paymentLog;
    }

    public int getClientCount() {
        return clients.size();
    }

    public int getOnlineCount() {
        return players.size();
    }

    public int getNetworkSlots() {
        return settings.getNetworkSlots();
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public AppHandler getAppHandler() {
        return (AppHandler) getHandler(ServerType.APP);
    }

    public Map<ServerType, Handler> getHandlers() {
        return handlers;
    }

    public File getUpdatesFolder() {
        return updatesFolder;
    }

    public File getDataFolder(String subFolder) {
        File file = new File(dataRepository.getBaseDirectory(), subFolder);
        file.mkdir();
        return file;
    }

    public long getInboundPacketCount() {
        return inboundPacketCount;
    }

    public long getOutboundPacketCount() {
        return outboundPacketCount;
    }

    public long getTotalPacketCount() {
        return inboundPacketCount + outboundPacketCount;
    }

    public int getPacketsPerSecond() {
        return packetsPerSecond.get();
    }

    public void incInboundPacketCount() {
        inboundPacketCount++;
        ppsCounter.getAndIncrement();
    }

    public NetHandler getNetHandler() {
        return netHandler;
    }

    public DataServer getDataServer() {
        return dataServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public Map<String, Pair<Client, RequestPacket>> getFailedLogins() {
        return failedLogins;
    }

    public void requestConnect(Player player, String server) {
        requestConnect(player, getBukkitClientHolder(server));
    }

    public void requestConnect(Player player, BukkitClient server) {
        if (!server.isActive()) {
            player.error(Message.SERVER_OFFLINE.format(server.getServerName()));
            return;
        }
        if (server.isLocked()) {
            if (server.getLockMessage() != null) {
                player.message(server.getLockMessage());
            } else {
                player.error(Message.SERVER_UNAVAILABLE.format(server.getServerName()));
            }
            return;
        }

        /**
         * Faction Server Queues
         */
        if (server.getServerName().equals("factionhardcore")) {
            FactionHandler h = getHandler(ServerType.FACTIONS);
            h.queueEntry(player, (FactionsClient) server);
            return;
        }

        /**
         * 15 second network-wide cooldown to join SMP servers
         */
        if (server.getType().isSmpType()) {
            int max = (int) ((double) server.getSlots() * settings.getSmpMaxOverflowPerc());
            if (!server.getServerName().equals("factionhardcore") && server.getOnlineCount() >= max) {
                player.error(Message.SERVER_FULL.format(server.getServerName()));
                return;
            }

            if (!player.hasPermission(Rank.ADMIN)) {
                if (player.lastFacJoinAttempt != 0) {
                    if ((System.currentTimeMillis() - player.lastFacJoinAttempt) < 10000) {
                        player.error("Please wait 10 seconds to attempt joining again.");
                        return;
                    }
                }
                player.lastFacJoinAttempt = System.currentTimeMillis();
            }
        }

//        player.message(Message.CONNECTING_TO_SERVER.format(server.getServerName()));
        player.connect(server);

        if (player.getRegion() != server.getRegion()) {
            ServerRegion r = server.getRegion();
            player.message("&cYou're connected using the wrong ip!");
            player.message("&cFor the best possible connection to this server, connect via &a" + r.getIp());
        }
    }

    public PlayerInfo getPlayerInfo(String name) {
        PlayerInfo info = null;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE name = '" + name + "'")) {
            if (rs.next()) {
                info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    public PlayerInfo getPlayerInfoByUuid(String uuid) {
        PlayerInfo info = null;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE uuid = '" + uuid + "'")) {
            if (rs.next()) {
                info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    public PlayerInfo getPlayerInfoAdv(String name) {
        PlayerInfo info = null;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE name LIKE '" + name + "%'")) {
            if (rs.next()) {
                info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    public void getPlayerInfo(String name, AsyncResult<PlayerInfo> result) {
        dataSource.getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE name = '" + name + "'")) {
                if (rs.next()) {
                    PlayerInfo info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
                    runTask(() -> result.done(info));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void getPlayerInfoByUuid(String uuid, AsyncResult<PlayerInfo> result) {
        dataSource.getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE uuid = '" + uuid + "'")) {
                if (rs.next()) {
                    PlayerInfo info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
                    runTask(() -> result.done(info));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void getPlayerInfoAdv(String name, AsyncResult<PlayerInfo> result) {
        dataSource.getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, uuid, name FROM players WHERE name LIKE '" + name + "%'")) {
                if (rs.next()) {
                    PlayerInfo info = new PlayerInfo(rs.getInt("id"), rs.getString("uuid"), rs.getString("name"));
                    runTask(() -> result.done(info));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void initConsole() {
        try {
            String line = null;
            while ((line = consoleReader.readLine(">")) != null) {
                List<String> args = new ArrayList<>();
                for (String arg : line.split("\\s+")) {
                    if (!arg.isEmpty()) {
                        args.add(arg);
                    }
                }
                if (args.isEmpty()) {
                    logger.info("Command not found.");
                    continue;
                }
                runTask(() -> {
                    String command = args.remove(0);
                    if (command.equalsIgnoreCase("reload")) {
                        reloadSettings();
                        logger.info("Successfully reloaded the settings.");
                    } else if (command.equalsIgnoreCase("stop")) {
                        logger.info("Shutting down...");
                        shutdown();
                    } else {
                        if (commandManager.isCommand(command)) {
                            commandManager.runConsoleCommand(command,
                                    args.toArray(new String[args.size()]));
                        } else {
                            logger.info("Command not found.");
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedPacket getServerCountList() {
        BufferedPacket buf = Protocol.SERVER_COUNT_LIST.construct();
        buf.writeInt(getOnlineCount());

        buf.writeString("lobby");
        LobbyHandler lobbyHandler = getHandler(ServerType.LOBBY);
        buf.writeInt(lobbyHandler.getOnlineCount());
        buf.writeInt(lobbyHandler.getSlots());

        for (Handler handler : handlers.values()) {
            if (handler instanceof GameLobbyHandler<?, ?>) {
                GameLobbyHandler<?, ?> h = (GameLobbyHandler<?, ?>) handler;
                ServerType type = h.getGameHandler().getLobbyType();
                if (type != null) {
                    buf.writeString(type.name().replace("_LOBBY", "").toLowerCase());
                    buf.writeInt(h.getOnlineCount());
                    buf.writeInt(h.getSlots());
                }
            } else if (handler instanceof NamedBukkitHandler<?>) {
                NamedBukkitHandler<?> h = (NamedBukkitHandler<?>) handler;
                if (!h.getClients().isEmpty()) {
                    buf.writeString(h.getClients().iterator().next().getType().name());
                    buf.writeInt(h.getOnlineCount());
                    buf.writeInt(h.getSlots());
                    for (BukkitClient client : h.getClients()) {
                        buf.writeString(client.getServerName());
                        buf.writeInt(client.getOnlineCount());
                        buf.writeInt(client.getSlots());
                    }
                }
            }
        }
        return buf;
    }

    public void shutdown() {
        dataServer.close();
        serviceManager.shutdownServices();
        System.exit(0);
    }

    public void runTask(Runnable task) {
        getExecutor().execute(() -> safeRun(task));
    }

    public ScheduledFuture<?> runTaskLater(Runnable task,
            long delay, TimeUnit unit) {
        return getExecutor().schedule(() -> safeRun(task), delay, unit);
    }

    public ScheduledFuture<?> runTaskTimer(Runnable task,
            long period, TimeUnit unit) {
        return runTaskTimer(() -> safeRun(task), 0, period, unit);
    }

    public ScheduledFuture<?> runTaskTimer(Runnable task, long initialDelay,
            long period, TimeUnit unit) {
        return getExecutor().scheduleAtFixedRate(() -> safeRun(task), initialDelay, period, unit);
    }

    public SingleThreadEventExecutor getExecutor() {
        return dataServer.getExecutor();
    }

    private void safeRun(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to run task on " + Thread.currentThread().getName(), t);
        }
    }
}
