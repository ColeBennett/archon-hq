package net.thearchon.hq;

import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.party.Party;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends PlayerInfo {

    /*
    Example MongoDB document
    {
      "_id": 1005,
      "uuid": "b3b45011-95de-4f3d-bc72-cd7b1632e532",
      "name": "BeastCoder",
      "rank": "OWNER",
      "ip": "127.0.0.1",
      "firstJoin": "2017-01-01",
      "playtime": 105,
      "lastSeen": "2017-01-01",
      "lastServer": "factionphantom"
    }
    */

    /**
     * Archon id number.
     */
    private final int id;
    /**
     * Mojang uuid of player.
     */
    private final String uuid;
    /**
     * Minecraft username of player.
     */
    private String name;
    /**
     * Ip address of player.
     */
    private String ipAddr;
    /**
     * Host if ip address.
     */
    private String host;
    /**
     * Archon rank of player.
     */
    private Rank rank;
    /**
     * Staff chat enabled/disabled.
     */
    private boolean staffChat;
    /**
     * Staff chat silent setting enabled/disabled.
     */
    private boolean staffSilent;
    /**
     * Current cap and swear warn counts.
     */
    private int capWarns, swearWarns;
    /**
     * Proxy player is connected to.
     */
    private BungeeClient proxy;
    /**
     * Current archon player is connected to.
     */
    private BukkitClient currentServer;
    /**
     * When player joined the network.
     */
    private final long sessionStart = System.currentTimeMillis();

    private int coins;
    private Party party;
    private boolean partyChatEnabled;
    private final Set<Party> partyInvites = new HashSet<>();

    private int unclaimedVotes;
    public long lastFacJoinAttempt;

    public Player(int id, String uuid, String name, BungeeClient proxy,
            String ipAddr, String host) {
        super(id, uuid, name);

        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.proxy = proxy;
        this.ipAddr = ipAddr;
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public int getCoins() {
        return coins;
    }

    public int getUnclaimedVotes() {
        return unclaimedVotes;
    }

    public String getAddress() {
        return ipAddr;
    }

    public String getHost() {
        return host;
    }

    public boolean hasStaffChatEnabled() {
        return staffChat;
    }

    public boolean hasStaffSilent() {
        return staffSilent;
    }

    public BungeeClient getProxy() {
        return proxy;
    }

    public BukkitClient getCurrentServer() {
        return currentServer;
    }

    public ServerRegion getRegion() {
        return proxy.getRegion();
    }

    public long getSessionStart() {
        return sessionStart;
    }

    public int getCapWarns() {
        return capWarns;
    }

    public int getSwearWarns() {
        return swearWarns;
    }

    public Party getParty() {
        return party;
    }

    public boolean getPartyChatEnabled() {
        return partyChatEnabled;
    }

    public Set<Party> getPartyInvites() {
        return partyInvites;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void setStaffChatEnabled(boolean staffChat) {
        this.staffChat = staffChat;
    }

    public void setStaffSilent(boolean staffSilent) {
        this.staffSilent = staffSilent;
    }

    public void setCurrentServer(BukkitClient client) {
        currentServer = client;
    }

    public void setCapWarns(int capWarns) {
        this.capWarns = capWarns;
    }
        
    public void setSwearWarns(int swearWarns) {
        this.swearWarns = swearWarns;
    }
    
    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setUnclaimedVotes(int unclaimedVotes) {
        this.unclaimedVotes = unclaimedVotes;
    }

    public void setProxy(BungeeClient proxy) {
        this.proxy = proxy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIpAddr(String addr) {
        this.ipAddr = addr;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public void setPartyChatEnabled(boolean enabled) {
        partyChatEnabled = enabled;
    }

    public boolean isStaff() {
        return Rank.hasPermission(getRank(), Rank.HELPER);
    }

    public boolean hasPermission(Rank check) {
        return Rank.hasPermission(getRank(), check);
    }

    public String getDisplayName() {
        return (rank == Rank.DEFAULT ? "&e" : rank.getColor()) + getName();
    }

    public void error(Message message) {
        error(message.toString());
    }

    public void error(String message) {
        message(Message.ERROR_COLOR + message);
    }

    public void message(Message message) {
        message(message.toString());
    }

    public void message(String message) {
        proxy.send(Protocol.MESSAGE.construct(uuid, message));
    }

    public void message(List<String> messages) {
        message(messages.toArray(new String[messages.size()]));
    }

    public void message(String... messages) {
        BufferedPacket buf = Protocol.MESSAGE.buffer(messages.length + 1);
        buf.writeString(uuid);
        for (String message : messages) {
            buf.writeString(message);
        }
        proxy.send(buf);
    }

    public void connect(BukkitClient client) {
        connect(client.getServerName());
    }

    public void connect(String server) {
        message(Message.CONNECTING_TO_SERVER.format(server));
        proxy.send(Protocol.SEND_PLAYER.construct(uuid, server));
    }

    public void disconnect(Message reason) {
        disconnect(reason.toString());
    }

    public void disconnect(String reason) {
        proxy.send(Protocol.KICK_PLAYER.construct(uuid, reason));
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj == this || (obj instanceof Player && ((Player) obj).id == id);
    }

    @Override
    public String toString() {
        return name;
    }
}
