package net.thearchon.hq.command.helper;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.command.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StafflistCommand extends Command {

    public StafflistCommand(Archon archon) {
        super(archon);
    }

//    private final Rank[] ranks = {
//            Rank.HELPER, Rank.HELPER_PLUS,
//            Rank.JR_MOD, Rank.SR_MOD,
//            Rank.ADMIN, Rank.OWNER};

    @Override
    public void execute(Player player, String[] args) {
        player.message(
                "&8" + Message.BAR,
                getStaffList(Rank.HELPER),
                getStaffList(Rank.HELPER_PLUS),
                getStaffList(Rank.TRIAL_MOD),
                getStaffList(Rank.JR_MOD),
                getStaffList(Rank.MOD),
                getStaffList(Rank.SR_MOD),
                getStaffList(Rank.HEAD_MOD),
                getStaffList(Rank.MANAGER),
                getStaffList(Rank.ADMIN),
                getStaffList(Rank.OWNER),
                "&8" + Message.BAR);
    }
    
    private String getStaffList(Rank rank) {
        Map<String, Long> names = new LinkedHashMap<>();

        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, TIMESTAMPDIFF(DAY, seen, NOW()) AS days FROM players WHERE rank = '" + rank.name() + "'")) {
            while (rs.next()) {
                names.put(rs.getString("name"), rs.getLong("days"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int i = 0, size = names.size();
        StringBuilder buf = new StringBuilder();
        buf.append(rank.getPrefix());
        buf.append(" &e(");
        buf.append(size);
        buf.append(") ");
        for (Entry<String, Long> entry : names.entrySet()) {
            String name = entry.getKey();
            buf.append(archon.hasPlayer(name) ? "&a" : "&7").append(name);
            if (!archon.hasPlayer(name)) {
                buf.append(" &c(");
                buf.append(entry.getValue());
                buf.append("d)");
            }
            if (i++ != (size - 1)) {
                buf.append("&a, ");
            }
        }
        names.clear();
        return buf.toString();
    }
}
