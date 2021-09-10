package net.thearchon.hq.punish;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.util.DateTimeUtil;
import net.thearchon.nio.Protocol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class PunishManager {

    private final Archon archon;
    private final Map<String, IpBanRecord> ipBans = new ConcurrentHashMap<>();
    private final Map<String, BanRecord> banRecords = new ConcurrentHashMap<>();
    private final Map<Integer, MuteRecord> muteRecords = new ConcurrentHashMap<>();

    public PunishManager(Archon archon) {
        this.archon = archon;

        cache();
    }
    
    public void cache() {
        archon.getLogger().info("Loading ban/ip/mute records...");

        long start = System.currentTimeMillis();
        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {

            // Load bans
            try (ResultSet rs = stmt.executeQuery("SELECT uuid, ban_time, ban_reason FROM players WHERE banned = true;")) {
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    String reason = rs.getString("ban_reason");
                    if (reason == null) {
                        reason = "";
                    }
                    if (rs.getObject("ban_time") != null) {
                        addRecord(uuid, new TempbanRecord(reason, rs.getLong("ban_time")));
                    } else {
                        addRecord(uuid, new BanRecord(reason));
                    }
                }
            }

            // Load ip bans
            try (ResultSet rs = stmt.executeQuery("SELECT ip_address, reason FROM ip_bans;")) {
                while (rs.next()) {
                    String ip = rs.getString("ip_address");
                    ipBans.put(ip, new IpBanRecord(ip, rs.getString("reason")));
                }
            }

            // Load mutes
            try (ResultSet rs = stmt.executeQuery("SELECT id, time, reason FROM mutes;")) {
                while (rs.next()) {
                    int id = (int) rs.getLong("id");
                    String reason = rs.getString("reason");
                    if (reason == null) {
                        reason = "";
                    }
                    if (rs.getObject("time") != null) {
                        muteRecords.put(id, new MuteRecord(reason, rs.getLong("time")));
                    } else {
                        muteRecords.put(id, new MuteRecord(reason, null));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = System.currentTimeMillis() - start;

        archon.getLogger().info("Loaded " + getTempbanCount() + " temp-ban records.");
        archon.getLogger().info("Loaded " + getPermbanCount() + " permanent ban records.");
        archon.getLogger().info("Loaded " + getIpBanCount() + " ip ban records.");
        archon.getLogger().info("Loaded " + getMuteCount() + " mute records.");
        archon.getLogger().info("Total: " + getBanCount());
        archon.getLogger().info("Took " + duration + " ms");
        
        removeExpiredTempbans();
    }

    public void warn(Player punisher, Player player, String reason) {
        if (reason.length() < 1) {
            reason = null;
        }

        player.message(
                "&4" + Message.BAR,
                "&c&lWARNING &cfrom " + punisher.getRank().getPrefix() + ' ' + punisher.getDisplayName(),
                "&d" + (reason != null ? reason : ""),
                "&4" + Message.BAR);

        archon.notifyStaff(punisher.getDisplayName() + " &7warned &e" + player.getDisplayName() + " &7" + (reason != null ? "with message: &c" + reason : "with no message"));

        if (punisher != null) {
            log(punisher.getId(), player.getId(), Punishment.WARNING, reason, null);
        } else {
            log(0, player.getId(), Punishment.WARNING, reason, null);
        }
    }

    public void kick(Player punisher, Player player, String reason) {
        if (reason.length() < 1) {
            reason = null;
        }

        player.disconnect("&7You have been kicked from &cTheArchon&7:\n\n&7Kicked by: &f"
                + (punisher != null ? punisher.getDisplayName() : "&6Network") + "\n&7Reason: &f" + (reason != null && reason.length() > 1 ? reason : "&cNone"));
        archon.notifyStaff(punisher.getDisplayName() + " &7kicked &e" + player.getDisplayName() + " &7" + (reason != null ? "with reason: &c" + reason : "from the network!"));

        if (punisher != null) {
            log(punisher.getId(), player.getId(), Punishment.KICK, reason, null);
        } else {
            log(0, player.getId(), Punishment.KICK, reason, null);
        }
    }

    public void ban(PlayerInfo info, String reason) {
        ban(info, reason, null);
    }

    public void ban(PlayerInfo info, String reason, Player punisher) {
        String uuid = info.getUuid();
        
        if (banRecords.containsKey(uuid)) {
            throw new IllegalStateException("Player already banned: " + info.getName());
        }

        banRecords.put(uuid, new BanRecord(reason));
        archon.getDataSource().execute("UPDATE players SET banned = true, ban_time = NULL, ban_reason = ? WHERE uuid = ?", reason, uuid);
        
        Player target = archon.getPlayerByName(info.getName());
        if (target != null) {
            target.disconnect("&7You have been banned from &cTheArchon&7:\n\n&7Banned by: &f"
                    + (punisher != null ? punisher.getDisplayName() : "&6Network") + "\n&7Reason: &f" + (reason != null && reason.length() > 1 ? reason : "The Ban Hammer has spoken!") + "\n\n&7Appeal at: &f&nwww.TheArchon.net");
        }

        archon.notifyStaff((punisher != null ? punisher.getDisplayName() : Message.CONSOLE)
                + " &7globally banned &e" + info.getName() + (reason != null && reason.length() > 1 ? " &7with reason: &c" + reason : "&7!"));

        if (punisher != null) {
            log(punisher.getId(), info.getId(), Punishment.BAN, reason, null);
        } else {
            log(0, info.getId(), Punishment.BAN, reason, null);
        }
    }

    public void tempban(PlayerInfo info, long endTime, String reason, String timeUnit, long duration) {
        tempban(info, endTime, reason, timeUnit, null, duration);
    }

    public void tempban(PlayerInfo info, long endTime, String reason, String timeUnit, Player punisher, long duration) {
        String uuid = info.getUuid();

        if (banRecords.containsKey(uuid)) {
            throw new IllegalStateException("Player already temp-banned: " + info.getName());
        }

        banRecords.put(uuid, new TempbanRecord(reason, endTime));
        archon.getDataSource().execute("UPDATE players SET banned = true, ban_time = ?, ban_reason = ? WHERE uuid = ?", endTime, reason, uuid);
        
        Player target = archon.getPlayerByName(info.getName());
        if (target != null) {
            target.disconnect("&7You have been banned from &cTheArchon&7:\n\n&7Banned by: &f"
                    + (punisher != null ? punisher.getDisplayName() : "&6Network") + "\n&7Reason: &f" + (reason != null && reason.length() > 1 ? reason : "The Ban Hammer has spoken!") + "\n\n&7Expires in: &e" + DateTimeUtil.formatTime((endTime - System.currentTimeMillis()) / 1000, false) + "\n&7Appeal at: &f&nwww.TheArchon.net");
        }

        archon.notifyStaff((punisher != null ? punisher.getDisplayName() : Message.CONSOLE)
                + " &7globally temp-banned &e" + info.getName() + " &7for &a" + timeUnit + (reason != null && reason.length() > 1 ? " &7with reason: &c" + reason : "&7!"));

        if (punisher != null) {
            log(punisher.getId(), info.getId(), Punishment.TEMPBAN, reason, duration);
        } else {
            log(0, info.getId(), Punishment.TEMPBAN, reason, duration);
        }
    }

    public void unban(PlayerInfo info) {
        unban(info, null);
    }
    
    public void unban(PlayerInfo info, Player punisher) {
        String uuid = info.getUuid();
        BanRecord record = banRecords.remove(uuid);
        if (record == null) {
            throw new IllegalStateException("Cannot unban player: " + info.getName());
        }
        archon.getDataSource().execute("UPDATE players SET banned = false, ban_time = NULL, ban_reason = NULL WHERE uuid = ?;", uuid);

        if (punisher != null) {
            archon.notifyStaff(punisher.getDisplayName() + " &7globally unbanned &e" + info.getName() + "&7!");
            log(punisher.getId(), info.getId(), Punishment.UNBAN, null, null);
        } else {
            log(0, info.getId(), Punishment.UNBAN, null, null);
        }
    }
    
    public void unbanAll(Player executor) {
        archon.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate("UPDATE players SET banned = false, ban_time = NULL, ban_reason = NULL WHERE banned = true && ban_reason NOT LIKE '%charging%';");
                archon.runTask(() -> {
                   if (updated > 0) {
                       banRecords.clear();
                       executor.message(Message.UNBANNED_ALL_PLAYERS.format(updated));
                   } else {
                       executor.error("No players to unban.");
                   }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * IP Bans
     */

    public void banIp(String addr, String reason) {
        banIp(addr, reason, null);
    }

    public void banIp(String ip, String reason, Player punisher) {
        ipBans.put(ip, new IpBanRecord(ip, reason));
        archon.getDataSource().execute("INSERT INTO ip_bans VALUES (?, ?);", ip, reason);
        for (Player p : archon.getPlayers()) {
            if (p.getAddress() != null && p.getAddress().equals(ip)) {
                p.disconnect("&7You have been banned from &cTheArchon&7:\n\n&7Banned by: &f" + (punisher != null ? punisher.getDisplayName() : "&6Network") + "\n&7Reason: &f" + (reason != null ? reason : "The Ban Hammer has spoken!") + "\n\n&7Appeal at: &f&nwww.TheArchon.net");
            }
        }
        archon.notifyStaff((punisher != null ? punisher.getDisplayName() : Message.CONSOLE) + " &7globally ip-banned &e" + obfuscateIp(ip) + (reason != null ? " &7with reason: &c" + reason : "&7!"));
    }

    public void unbanIp(String ip, Player punisher) {
        IpBanRecord record = ipBans.remove(ip);
        if (record == null) {
            throw new IllegalStateException("Cannot unban ip: " + ip);
        }
        archon.getDataSource().execute("DELETE FROM ip_bans WHERE ip_address = ?", ip);

        if (punisher != null) {
            archon.notifyStaff(punisher.getDisplayName() + " &7globally unbanned ip &e" + obfuscateIp(ip) + "&7!");
        }
    }

    private String obfuscateIp(String ip) {
        StringBuilder buf = new StringBuilder(ip.length());
        boolean first = true;
        for (char c : ip.toCharArray()) {
            if (first) {
                buf.append(c);
                if (c == '.') {
                    first = false;
                }
                continue;
            }
            buf.append(c == '.' ? c : '*');
        }
        return buf.toString();
    }

    public void tempbanExpired(String uuid) {
        BanRecord record = banRecords.remove(uuid);
        if (record == null) {
            throw new IllegalStateException("Cannot unban expired tempban record: " + uuid);
        }
        archon.getDataSource().execute("UPDATE players SET banned = false, ban_time = NULL, ban_reason = NULL WHERE uuid = ?", uuid);
    }
    
    public void removeExpiredTempbans() {
        Iterator<Entry<String, BanRecord>> itr = banRecords.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, BanRecord> entry = itr.next();
            if (entry.getValue() instanceof TempbanRecord) {
                TempbanRecord record = (TempbanRecord) entry.getValue();
                if (record.getTime() < System.currentTimeMillis()) {
                    String uuid = entry.getKey();
                    itr.remove();
                    archon.getDataSource().execute("UPDATE players SET banned = false, ban_time = NULL, ban_reason = NULL WHERE uuid = ?", uuid);
                    archon.getLogger().info("[REMOVED EXPIRED BAN RECORD] Player: " + uuid);
                }
            }
        }
    }

    /**
     * Mutes
     */

    public void mute(PlayerInfo info, long endTime, String reason, String timeUnit, long duration) {
        mute(info, endTime, reason, timeUnit, null, duration);
    }

    public void mute(PlayerInfo info, long endTime, String reason, String timeUnit, Player punisher, long duration) {
        int id = info.getId();
        if (muteRecords.containsKey(id)) {
            throw new IllegalStateException("Player already muted: " + info.getName());
        }

        muteRecords.put(id, new MuteRecord(reason, endTime));
        archon.getDataSource().execute("INSERT INTO mutes VALUES (?, ?, ?)", id, endTime, reason);

        archon.notifyStaff((punisher != null ? punisher.getDisplayName() : Message.CONSOLE) + " &7globally muted &e" + info.getName() + " &7for &a" + timeUnit + (reason.length() > 1 ? " &7with reason: &c" + reason : "&7!"));

        Player target = archon.getPlayer(info.getId());
        if (target != null) {
            target.getProxy().send(Protocol.DISABLE_CHAT.construct(target.getUuid()));
            target.message("&c&lYou have been muted!"
                    + "\n&7Muted by: &f" + (punisher != null ? punisher.getDisplayName() : "&6Network")
                    + "\n&7Reason: &f" + (reason != null && reason.length() > 1 ? reason : "None")
                            + "\n&7Expires in: &e" + DateTimeUtil.formatTime((endTime - System.currentTimeMillis()) / 1000, false)
                            + "\n&7Appeal at: &f&nwww.TheArchon.net");
        }


        if (punisher != null) {
            log(punisher.getId(), info.getId(), Punishment.MUTE, reason, duration);
        } else {
            log(0, info.getId(), Punishment.MUTE, reason, duration);
        }
    }

    public void unmute(PlayerInfo info) {
        unmute(info, null);
    }

    public void unmute(PlayerInfo info, Player punisher) {
        int id = info.getId();
        MuteRecord record = muteRecords.remove(id);
        if (record == null) {
            throw new IllegalStateException("Cannot unmute player: " + info.getName());
        }
        archon.getDataSource().execute("DELETE FROM mutes WHERE id = ?", id);

        Player target = archon.getPlayer(info.getId());
        if (target != null) {
            target.getProxy().send(Protocol.ENABLE_CHAT.construct(target.getUuid()));
        }

        if (punisher != null) {
            archon.notifyStaff(punisher.getDisplayName() + " &7globally unmuted &e" + info.getName() + "&7!");
            log(punisher.getId(), info.getId(), Punishment.UNMUTE, null, null);
        } else {
            log(0, info.getId(), Punishment.UNMUTE, null, null);
        }
    }

    public void removeExpiredMuteRecord(int id) {
        muteRecords.remove(id);
        archon.getDataSource().execute("DELETE FROM mutes WHERE id = ?", id);
        archon.getLogger().info("[REMOVED EXPIRED MUTE RECORD] Player: " + id);

        Player player = archon.getPlayer(id);
        if (player != null) {
            player.getProxy().send(Protocol.ENABLE_CHAT.construct(player.getUuid()));
        }
    }

    public void removeExpiredMutes() {
        Iterator<Entry<Integer, MuteRecord>> itr = muteRecords.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, MuteRecord> entry = itr.next();
            Long time = entry.getValue().getTime();
            if (time != null && time < System.currentTimeMillis()) {
                int id = entry.getKey();
                itr.remove();
                archon.getDataSource().execute("DELETE FROM mutes WHERE id = ?", id);
                archon.getLogger().info("[REMOVED EXPIRED MUTE RECORD] Player: " + id);

                Player player = archon.getPlayer(id);
                if (player != null) {
                    player.getProxy().send(Protocol.ENABLE_CHAT.construct(player.getUuid()));
                }
            }
        }
    }

    public boolean hasMuteRecord(int id) {
        return muteRecords.containsKey(id);
    }

    public boolean hasMuteRecord(PlayerInfo info) {
        return hasMuteRecord(info.getId());
    }

    public TempbanRecord getTempbanRecord(String uuid) {
        return (TempbanRecord) banRecords.get(uuid);
    }

    public BanRecord getBanRecord(String uuid) {
        return banRecords.get(uuid);
    }

    public BanRecord getRecord(String uuid) {
        return banRecords.get(uuid);
    }

    public IpBanRecord getIpBanRecord(String ip) {
        return ipBans.get(ip);
    }

    public MuteRecord getMuteRecord(int id) {
        return muteRecords.get(id);
    }

    public void addRecord(String uuid, BanRecord record) {
        banRecords.put(uuid, record);
    }

    public boolean hasRecord(String uuid) {
        return banRecords.containsKey(uuid);
    }

    public boolean hasRecord(PlayerInfo info) {
        return banRecords.containsKey(info.getUuid());
    }

    public boolean hasIpRecord(String ip) {
        return ipBans.containsKey(ip);
    }
    
    public int getBanIpCount() {
        return ipBans.size();
    }

    public Map<String, BanRecord> getBanRecords() {
        return banRecords;
    }

    public Map<Integer, MuteRecord> getMuteRecords() {
        return muteRecords;
    }
    
    public int getBanCount() {
        return banRecords.size() + ipBans.size();
    }
    
    public int getPermbanCount() {
        int count = 0;
        for (BanRecord record : banRecords.values()) {
            if (!(record instanceof TempbanRecord)) {
                count++;
            }
        }
        return count;
    }
    
    public int getTempbanCount() {
        int count = 0;
        for (BanRecord record : banRecords.values()) {
            if (record instanceof TempbanRecord) {
                count++;
            }
        }
        return count;
    }

    public int getIpBanCount() {
        return ipBans.size();
    }

    public int getMuteCount() {
        return muteRecords.size();
    }

    private void log(int punisher, int punished, Punishment punishment, String reason, Long duration) {
        archon.getDataSource().execute("INSERT INTO punishment_log VALUES(NULL, ?, ?, ?, NOW(), ?, ?);", punisher, punished, punishment.name(), duration, reason);
    }
}
