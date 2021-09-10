package net.thearchon.hq.app;

import net.thearchon.hq.client.Client;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PurchaseLog {

    private final AppHandler handler;

    private double total;
    private double month;
    private double week;
    private double today;
    private double hour;
    private double chargeback;

    private double factions;
    private double minigames;
    private double prison;

    PurchaseLog(AppHandler handler) {
        this.handler = handler;

//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            double totalPrev = total;
//            long time = update();
//            Archon.getInstance().getLogger().warning("PurchaseLog.update() took " + time + " ms - " + Thread.currentThread().getName()); // TODO remove
//            if (total != totalPrev) {
//                updateCumulative();
//            }
//        }, 0, 15, TimeUnit.SECONDS);

//        handler.getParent().getDataServer().getExecutor().scheduleAtFixedRate(new Runnable() {
//            int lastHour = -1;
//
//            @Override
//            public void run() {
//                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
//                int current = cal.get(Calendar.HOUR_OF_DAY);
//                if (lastHour == -1) {
//                    lastHour = current;
//                }
//                if (lastHour != current) {
//                    hour = 0;
//                    PurchaseLog.this.handler.sendAll(new PacketPurchaseStat("moneyHour", 0d));
//                    if (current == 0) {
//                        today = 0;
//                        PurchaseLog.this.handler.sendAll(new PacketPurchaseStat("moneyToday", 0d));
//                    }
//                }
//                lastHour = current;
//            }
//        }, 0, 1, TimeUnit.SECONDS);
    }

    private final String QUERY =
            "SELECT (SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE') AS total, " +

                    "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE'" +
                    " && YEAR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = YEAR(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && MONTH(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = MONTH(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))) AS month, " +

                    "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE'" +
                    " && YEAR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = YEAR(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && MONTH(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = MONTH(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && YEARWEEK(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = YEARWEEK(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))) AS week, " +

                    "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE'" +
                    " && YEAR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = YEAR(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && MONTH(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = MONTH(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && DAY(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = DAY(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))) AS today, " +

                    "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE'" +
                    " && YEAR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = YEAR(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && MONTH(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = MONTH(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && DAY(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = DAY(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))" +
                    " && HOUR(CONVERT_TZ(date, 'UTC', 'America/Los_Angeles')) = HOUR(CONVERT_TZ(UTC_TIMESTAMP, 'UTC', 'America/Los_Angeles'))) AS hour, " +

            "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE' && server LIKE 'faction%') AS factions, " +
            "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE' && server = 'minigames') AS minigames, " +
            "(SELECT SUM(price) FROM purchases WHERE status = 'COMPLETE' && server LIKE '%prison%') AS prison, " +
            "(SELECT SUM(price) FROM purchases WHERE status = 'CHARGEBACK' || status = 'REFUND') AS chargeback " +
            "FROM purchases LIMIT 1";

    private long update() {
        long start = System.currentTimeMillis();
        try (Connection conn = handler.getParent().getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(QUERY)) {
            if (rs.next()) {
                total = rs.getDouble("total");
                month = rs.getDouble("month");
                week = rs.getDouble("week");
                today = rs.getDouble("today");
                hour = rs.getDouble("hour");
                chargeback = rs.getDouble("chargeback");
                factions = rs.getDouble("factions");
                minigames = rs.getDouble("minigames");
                prison = rs.getDouble("prison");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() - start;
    }

    public double getTotal() {
        return total;
    }

    public double getMonth() {
        return month;
    }

    public double getWeek() {
        return week;
    }

    public double getToday() {
        return today;
    }

    public double getHour() {
        return hour;
    }

    public double getFactions() {
        return factions;
    }

    public double getMinigames() {
        return minigames;
    }

    public double getPrison() {
        return prison;
    }

    public void sendStats(Client client) {
//        client.send(new PacketPurchaseStat("moneyChargeback", chargeback));
//        client.send(new PacketPurchaseStat("moneyTotal", total));
//        client.send(new PacketPurchaseStat("moneyMonth", month));
//        client.send(new PacketPurchaseStat("moneyWeek", week));
//        client.send(new PacketPurchaseStat("moneyToday", today));
//        client.send(new PacketPurchaseStat("moneyHour", hour));
//
//        client.send(new PacketPurchaseStat("moneyFactions", factions));
//        client.send(new PacketPurchaseStat("moneyMinigames", minigames));
//        client.send(new PacketPurchaseStat("moneyPrison", prison));
    }

    private void updateCumulative() {
//        handler.sendAll(new PacketPurchaseStat("moneyChargeback", chargeback));
//        handler.sendAll(new PacketPurchaseStat("moneyTotal", total));
//        handler.sendAll(new PacketPurchaseStat("moneyMonth", month));
//        handler.sendAll(new PacketPurchaseStat("moneyWeek", week));
//        handler.sendAll(new PacketPurchaseStat("moneyToday", today));
//        handler.sendAll(new PacketPurchaseStat("moneyHour", hour));
//
//        handler.sendAll(new PacketPurchaseStat("moneyFactions", factions));
//        handler.sendAll(new PacketPurchaseStat("moneyMinigames", minigames));
//        handler.sendAll(new PacketPurchaseStat("moneyPrison", prison));
    }

    public void log(String server, String status, double price) {
//        if (status.equals("CHARGEBACK") || status.equals("REFUND")) {
//            total -= price;
//            today -= price;
//            hour -= price;
//            updateCumulative();
//            return;
//        } else {
//            total += price;
//            today += price;
//            hour += price;
//            updateCumulative();
//        }
//        if (server.contains("faction")) {
//            factions += price;
//            handler.sendAll(new PacketPurchaseStat("moneyFactions", factions));
//        } else if (server.equals("global")) {
//            minigames += price;
//            handler.sendAll(new PacketPurchaseStat("moneyMinigames", minigames));
//        } else if (server.contains("prison")) {
//            prison += price;
//            handler.sendAll(new PacketPurchaseStat("moneyPrison", prison));
//        }
    }
}
