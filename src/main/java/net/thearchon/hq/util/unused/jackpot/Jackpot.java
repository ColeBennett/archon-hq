package net.thearchon.hq.util.unused.jackpot;

import net.thearchon.hq.Player;

import java.util.List;

public class Jackpot {

    private final JackpotManager manager;

    private final String name;
    private final int threshold;
    private List<Participant> participants;

    Jackpot(JackpotManager manager, String name, int threshold) {
        this.manager = manager;
        this.name = name;
        this.threshold = threshold;
    }

    public String getName() {
        return name;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getParticipantCount() {
        return participants.size();
    }

    void loadParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void addTicket(Player player, String server) {
//        String uuid = player.getUuid();
//        participants.add(new Participant(uuid, player.getName(), server));
//
//        if (participants.size() >= threshold) {
//            manager.broadcastToFactions(Message.PREFIX + "&aThe &e" + name + " &ahas ended! Tickets have been reset.");
//
//            Participant winner = participants.get(new Random().nextInt(participants.size()));
//            manager.broadcastToFactions(Arrays.asList(
//                    "&d" + Message.BAR,
//                    "                       &c[ &a&lARCHON JACKPOTS &c]",
//                    "",
//                    "&6" + winner.getName() + " &dwon the &e&o" + name + " &don &b" + winner.getServer() + "&d!",
//                    "",
//                    "&3Use &e/jackpot &3or &e/jp &3to view active jackpots.",
//                    "&d" + Message.BAR
//            ));
//
//            DataSource ds = Archon.getInstance().getDataSource();
//            try (Connection conn = ds.getConnection();
//                 Statement stmt = conn.createStatement()) {
//                List<Packet> out = new ArrayList<>();
//
//                Player p = Archon.getInstance().getPlayerByUuid(winner.getUuid());
//                if (p != null && p.getCurrentServer().getType() == ServerType.FACTIONS) {
//                    out.add(Protocol.JACKPOT_REWARD.construct(p.getUuid(), name, "winner"));
//                }
//
//                stmt.addBatch(ds.inject(true, "INSERT INTO jackpot_pending VALUES (?, ?, ?, 'winner')",
//                        winner.getUuid(), name, winner.getServer()));
//
//                for (Participant u : participants) {
//                    if (u.equals(winner)) continue;
//                    try {
//                        stmt.addBatch(ds.inject(true, "INSERT INTO jackpot_pending VALUES (?, ?, ?, 'participant')",
//                                u.getUuid(), name, u.getServer()));
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                stmt.executeBatch();
//
//                Set<Participant> each = new HashSet<>(participants);
//                for (Participant u : each) {
//                    if (u.equals(winner)) continue;
//                    p = Archon.getInstance().getPlayerByUuid(u.getUuid());
//                    if (p != null && p.getCurrentServer().getType() == ServerType.FACTIONS) {
//                        out.add(Protocol.JACKPOT_REWARD.construct(p.getUuid(), name, "participant"));
//                    }
//                }
//
//                manager.getDb().execute("DELETE FROM participants WHERE jackpot = ?", name);
//                participants.clear();
//
//                out.add(Protocol.JACKPOT_UPDATE.construct(name, 0, uuid));
//                for (Packet packet : out) {
//                    Archon.getInstance().sendAll(packet, ServerType.FACTIONS);
//                }
//
//                ResultSet rs = stmt.executeQuery(ds.inject(true, "SELECT id FROM players WHERE uuid = ?", winner.getUuid()));
//                if (rs.next()) {
//                    stmt.execute(ds.inject(true, "INSERT INTO jackpot_log (player, jackpot, server, time) VALUES (?, ?, ?, NOW())",
//                            rs.getInt("id"), name, winner.getServer()));
//                }
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } else {
//            manager.getDb().execute("INSERT INTO participants VALUES (?, ?, ?, ?)", uuid, player.getName(), name, server);
//            Archon.getInstance().sendAll(Protocol.JACKPOT_UPDATE.construct(name, getParticipantCount(), uuid), ServerType.FACTIONS);
//        }
    }
}
