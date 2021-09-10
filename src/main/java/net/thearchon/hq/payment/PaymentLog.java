package net.thearchon.hq.payment;

import net.thearchon.hq.*;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.payment.actions.*;
import net.thearchon.hq.service.buycraft.CommandListener;
import net.thearchon.hq.service.buycraft.PackageCommand;
import net.thearchon.hq.util.io.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PaymentLog implements CommandListener {

    public static final String MINIGAMES_SERVER_NAME = "minigames";

    private final Archon server;
    private final Map<String, PaymentAction> actions = new HashMap<>();

    private Database db;

    public PaymentLog(Archon archon) {
        this.server = archon;

        Settings c = archon.getSettings();
        db = new Database(c.getMysqlHost(), c.getMysqlPort(), c.getMysqlDatabase(), c.getMysqlUsername(), c.getMysqlPassword());
        archon.runTaskTimer(() -> {
            if ((System.currentTimeMillis() - db.getLastActivity()) >= 60000) {
                db.executeQuery("SELECT 1;");
            }
        }, 15, 15, TimeUnit.SECONDS);

        actions.put("rank", new RankAction());
        actions.put("givecoins", new GiveCoinsAction());
        actions.put("givewheelspin", new GiveWheelSpinAction());
        actions.put("givetokens", new GiveTokensAction());
        actions.put("jpgive", new GiveJackpotTicketsAction());
        
//        actions.put("subadd", new SubscriptionAdd(archon));
//        actions.put("subrem", new SubscriptionRemove(archon));

        archon.getDataSource().execute("CREATE TABLE IF NOT EXISTS accepts (transaction VARCHAR(64) NOT NULL PRIMARY KEY, uuid VARCHAR(36) NOT NULL, accepted BOOLEAN DEFAULT NULL);");

        archon.getCommandManager().register("accept", new Command() {
            @Override
            public void execute(Player player, String[] args) {
                server.getDataSource().getConnection(conn -> {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT transaction FROM accepts WHERE uuid = '" + player.getUuid() + "' && accepted IS NULL LIMIT 1")) {
                        if (rs.next()) {
                            String transaction = rs.getString("transaction");
                            stmt.execute("UPDATE accepts SET accepted = true WHERE transaction = '" + transaction + "'");
                            player.message("&a&lAccepted purchase! &7(" + transaction + ")");
                        } else {
                            player.message("&cNo pending purchases found.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, Rank.DEFAULT);
        archon.getCommandManager().register("deny", new Command() {
            @Override
            public void execute(Player player, String[] args) {
                server.getDataSource().getConnection(conn -> {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT transaction FROM accepts WHERE uuid = '" + player.getUuid() + "' && accepted IS NULL LIMIT 1")) {
                        if (rs.next()) {
                            String transaction = rs.getString("transaction");
                            stmt.execute("UPDATE accepts SET accepted = false WHERE transaction = '" + transaction + "'");
                            player.message("&c&lDenied purchase! &7(" + transaction + ")");
                        } else {
                            player.message("&cNo pending purchases found.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, Rank.DEFAULT);

//        archon.runTaskTimer(() -> {
//            archon.getDataSource().getConnection(conn -> {
//                try (Statement stmt = conn.createStatement();
//                     ResultSet rs = stmt.executeQuery("SELECT uuid FROM accepts")) {
//                    if (rs.next()) {
//                        String transaction = rs.getString("transaction");
//                        stmt.execute("UPDATE accepts SET accepted = false WHERE transaction = '" + transaction + "'");
//                        player.message("&c&lDenied purchase! &7(" + transaction + ")");
//                    } else {
//                        player.message("&cNo pending purchases found.");
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            });
//        }, 30, TimeUnit.SECONDS);
    }

    @Override
    public void commandReceived(PackageCommand command) {
        String actionName = command.getLabel();
        server.getLogger().info("commandReceived(): actionName=" + actionName + ", user=" + command.getUsername() + ", uuid=" + command.getUuid() + ", args=" + Arrays.toString(command.getArgs()));
        if (actionName.equalsIgnoreCase("logpurchase")) {
            Payment payment = log(command.getArgs(), command.getUuid(), MINIGAMES_SERVER_NAME);
            if (payment != null) {
                if (payment.getStatus() == PaymentStatus.CHARGEBACK) {
                    PlayerInfo info = server.getPlayerInfoByUuid(command.getUuid());
                    if (info != null) {
//                        server.getPunishManager().ban(info, Message.CHARGEBACK_BAN.toString());
                    }
                }
            }
        } else {
            PaymentAction action = actions.get(actionName);
            if (action != null) {
                action.handle(command);
            } else {
                // If it is not a registered action, check to run it as an AHQ command.
                Command cmd = server.getCommandManager().getCommand(actionName);
                if (cmd != null) {
                    cmd.execute(server.getCommandManager().getConsoleSender(), command.getArgs());
                }
            }
        }
    }

    public Payment log(String[] parts, String uuid, String serverName) {
        String status = parts[0];
        String id = parts[1];
        double price = Double.parseDouble(parts[2]);
        String currency = parts[3];
        String username = parts[4];
        String time = parts[9];
        String email = parts[11];
        String ip = parts[10];

//        if (email.isEmpty()) {
//            email = null;
//        }
//        if (ip.isEmpty()) {
//            ip = null;
//        }

        String packageName = "";
        for (int i = 12; i < parts.length; i++) {
            packageName += parts[i] + ' ';
        }
        packageName = packageName.trim();

        if (packageName.contains("VIP [1 Month]")) {
            packageName = "VIP";
        } else if (packageName.contains("VIP+ [3 Months]")) {
            packageName = "VIP+";
        } else if (packageName.contains("MVP [6 Months]")) {
            packageName = "MVP";
        } else if (packageName.contains("MVP+ [Lifetime]")) {
            packageName = "MVP+";
        }

        String[] dateParts = parts[8].split("/");
        String sqlDate = dateParts[2] + '-' + dateParts[1] + '-' + dateParts[0];

        PaymentStatus statusType;
        if (status.equalsIgnoreCase("INITIAL") || status.equalsIgnoreCase("EXPIRY")) {
            statusType = PaymentStatus.COMPLETE;
        } else {
            statusType = PaymentStatus.valueOf(status);
        }

        Payment payment = new Payment(id, statusType, price, currency, packageName, sqlDate, time, email, ip, serverName, uuid, username);
//        server.getLogger().warning(payment.toString()); // TODO temp
        logToDb(payment);

        if (status.equalsIgnoreCase("INITIAL")) {
            PlayerInfo info = server.getPlayerInfo(username);
            if (info != null) {
                Player player = server.getPlayer(info.getId());
                if (player != null) {
                    if (!player.getAddress().equals(ip)) {
                        server.getDataSource().execute("INSERT INTO accepts (transaction, uuid) VALUES (?, ?)", id, info.getUuid());
                        notifyPendingPurchase(player, serverName, price);
                    }
                } else {
                    server.getDataSource().getConnection(conn -> {
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT ip_address FROM players WHERE id = " + info.getId())) {
                            if (rs.next()) {
                                String foundIp = rs.getString("ip_address");
                                if (!foundIp.equals(ip)) {
                                    server.getDataSource().execute("INSERT INTO accepts (transaction, uuid) VALUES (?, ?)", id, info.getUuid());
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                server.getLogger().warning("PaymentLog.log(): Player not found in database: " + username);
            }
        }
        if (payment.getStatus() == PaymentStatus.CHARGEBACK) {
            PlayerInfo info = server.getPlayerInfo(username);
            if (info != null) {
                server.getDataSource().getConnection(conn -> {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT uuid, accepted FROM accepts WHERE transaction = '" + id + "'")) {
                        if (rs.next()) {
                            Object accepted = rs.getObject("accepted");
                            if (accepted == null || ((Boolean) accepted)) { // Ban the user if the purchase was accepted by the player and charged back
                                server.getPunishManager().ban(info, Message.CHARGEBACK_BAN.toString());
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                server.getLogger().warning("PaymentLog.log(): Player not found in database: " + username);
            }
        }
        return payment;
    }

    public void checkPendingAccepts(Player player) {
        server.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT transaction FROM accepts WHERE uuid = '" + player.getUuid() + "' && accepted IS NULL;")) {
                List<String> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(rs.getString("transaction"));
                }

                for (String tx : transactions) {
                    try (ResultSet rs2 = stmt.executeQuery("SELECT price, server FROM purchases WHERE transaction = '" + tx + "';")) {
                        if (rs2.next()) {
                            notifyPendingPurchase(player, rs2.getString("server"), rs2.getDouble("price"));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void notifyPendingPurchase(Player player, String server, double price) {
        player.message(
                "&3" + Message.BAR,
                "&bDid you make a recent purchase of &a$" + price + "&b for server &7" + server + "&b?",
                "&bType &a&l/accept &bor &c&l/deny &bto confirm this purchase made with your username.",
                "&4WARNING: &cIf you did not make this purchase it could possibly be charged back by another user and result in you being banned from the network.",
                "&3" + Message.BAR);
    }

    private String append(String existing, String newValue) {
        Set<String> packages = new LinkedHashSet<>();
        if (existing.contains(" | ")) {
            String[] spl = existing.split("\\|");
            for (String s : spl) {
                if (!s.isEmpty()) {
                    packages.add(s.trim());
                }
            }
        } else {
            packages.add(existing);
        }
        packages.add(newValue);
        StringBuilder ser = new StringBuilder();
        Iterator<String> itr = packages.iterator();
        while (itr.hasNext()) {
            ser.append(itr.next());
            if (itr.hasNext()) {
                ser.append(" | ");
            }
        }
        return ser.toString();
    }

    private void logToDb(Payment payment) {
        server.getDataSource().getConnection(conn -> {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT server, package FROM purchases WHERE transaction = '" + payment.getTransactionId() + "';")) {
                boolean exists = false;
                String serverName = null, packageName = null;
                if (rs.next()) {
                    exists = true;
                    serverName = rs.getString("server");
                    packageName = rs.getString("package");
                }
                if (exists) {
                    String serverList = append(serverName, payment.getServerName());
                    String packageList = append(packageName, payment.getPackageName());
                    stmt.executeUpdate(server.getDataSource().inject(true, "UPDATE purchases SET status = ?, server = ?, package = ? WHERE transaction = ?;",
                            payment.getStatus(),
                            serverList,
                            packageList,
                            payment.getTransactionId()));
                } else {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO purchases VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = ?;")) {
                        ps.setString(1, payment.getTransactionId());
                        ps.setString(2, payment.getStatus().name());
                        ps.setString(3, payment.getUsername());
                        ps.setString(4, payment.getServerName());
                        ps.setString(5, payment.getDate() + ' ' + payment.getTime());
                        ps.setDouble(6, payment.getPrice());
                        ps.setString(7, payment.getCurrency());
                        ps.setString(8, payment.getPackageName());
                        ps.setString(9, payment.getIpAddress());
                        ps.setString(10, payment.getEmail());
                        ps.setString(11, payment.getStatus().name());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // logpurchase INITIAL {transaction} {price} {currency} {name} {packageId} {packagePrice} {packageExpiry} {date} {time} {email} {ip} {packageName}
}
