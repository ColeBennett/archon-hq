package net.thearchon.hq.util.unused.jackpot;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.handler.factions.FactionsClient;
import net.thearchon.hq.util.io.AsyncDatabase;
import net.thearchon.hq.util.io.Database;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JackpotManager {

    private final Archon server;
    private final AsyncDatabase db;

    private final Jackpot[] jackpots = {
            new Jackpot(this, "$175 Rank Jackpot", 75),
            new Jackpot(this, "$350 Rank Jackpot", 150),
            new Jackpot(this, "$600 Rank Jackpot", 250)};

    public JackpotManager(Archon server) {
        this.server = server;

        server.getDataSource().execute("CREATE TABLE IF NOT EXISTS jackpot_pending (" +
                "uuid VARCHAR(36) NOT NULL," +
                " jackpot VARCHAR(32) NOT NULL," +
                " server VARCHAR(32)  NOT NULL, " +
                " type VARCHAR(11) NOT NULL)");
        server.getDataSource().execute("CREATE TABLE IF NOT EXISTS jackpot_log (" +
                "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                " player INT UNSIGNED NOT NULL," +
                " jackpot VARCHAR(32) NOT NULL," +
                " server VARCHAR(32) NOT NULL," +
                " time DATETIME NOT NULL)");

        File dir = new File("data/jackpot");
        db = new AsyncDatabase(new Database(new File(dir, "participants.db")));
        db.sync().execute("CREATE TABLE IF NOT EXISTS participants (" +
                "uuid TEXT, name TEXT, jackpot TEXT, server TEXT)");

        for (Jackpot jp : jackpots) {
            List<Participant> participants = new ArrayList<>();
            try {
                ResultSet rs = db.sync().executeQuery("SELECT uuid, name, server FROM participants WHERE jackpot = ?", jp.getName());
                while (rs.next()) {
                    participants.add(new Participant(
                            rs.getString("uuid"),
                            rs.getString("name"),
                            rs.getString("server")));
                }
                rs.close();
                jp.loadParticipants(participants);
            } catch (SQLException e) {
                server.getLogger().log(Level.WARNING, "Failed to set jackpot participants: " + jp.getName(), e);
            }
        }

        // TODO: Disabled for now
//        archon.runTaskTimer(() -> {
//            List<String> msg = new ArrayList<>();
//            msg.add("&8" + Lang.BAR);
//            msg.add(Lang.PREFIX + "&6Active Jackpots &7(&e/jackpot or /jp&7)");
//            int total = 0;
//            for (Jackpot jp : jackpots) {
//                total += jp.getParticipantCount();
//                int perc = (int) (((double) jp.getParticipantCount() / (double) jp.getThreshold()) * 100D);
//                msg.add("&7- &3" + jp.getName() + ": &a" + jp.getParticipantCount() + "&7/&a" + jp.getThreshold() + " &7(" + perc + "%)");
//            }
//            msg.add("");
//            if (total == 1) {
//                msg.add("&3There is &b1 &3player participating in global jackpots.");
//            } else {
//                msg.add("&6There are &b" + total + " &6players participating in global jackpots.");
//            }
//            msg.add("&8" + Lang.BAR);
//            broadcastToFactions(msg);
//        }, 1, 5, TimeUnit.MINUTES);
    }

    public AsyncDatabase getDb() {
        return db;
    }

    public void sendList(FactionsClient client) {
//        BufferedPacket buf = Protocol.JACKPOT_LIST.buffer(jackpots.length * 3);
//        for (Jackpot jp : jackpots) {
//            buf.writeString(jp.getName());
//            buf.writeInt(jp.getParticipantCount());
//            buf.writeInt(jp.getThreshold());
//        }
//        client.send(buf);
    }

    public void addTicket(Player player, String jackpot, String server) {
        for (Jackpot jp : jackpots) {
            if (jp.getName().equalsIgnoreCase(jackpot)) {
                jp.addTicket(player, server);
                break;
            }
        }
    }

    public void broadcastToFactions(String message) {
        for (FactionsClient client : server.getClients(FactionsClient.class)) {
            if (client.getServerName().equals("factioncannon")) {
                continue;
            }
            client.broadcast(message);
        }
    }

    public void broadcastToFactions(List<String> messages) {
        for (FactionsClient client : server.getClients(FactionsClient.class)) {
            client.broadcast(messages);
        }
    }
}
