package net.thearchon.hq.util.unused.subscriptions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SubscriptionManager {

    private final Archon archon;
    private final Map<String, SubscriptionInfo> subscriptions = new HashMap<>();

    public SubscriptionManager(Archon archon) {
        this.archon = archon;

        // Load subscriptions
        try (Connection conn = archon.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, subscription, ABS(UNIX_TIMESTAMP(expiry) - UNIX_TIMESTAMP()) FROM subscriptions")) {
            while (rs.next()) {
                String uuid = rs.getString(1);
                Subscription sub = Subscription.valueOf(rs.getString(2));
                subscriptions.put(uuid, new SubscriptionInfo(sub, rs.getLong(3)));
            }
        } catch (SQLException e) {
            archon.getLogger().log(Level.SEVERE, "Failed to cache subscriptions", e);
        }
    }

    /**
     * Update a user's subscription.
     * @param uuid uuid of player
     * @param subscription subscription to set
     * @return true if the user already had an active subscription
     */
    public boolean update(String uuid, String name, Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("subscription");
        }

        boolean wasActive = false;
        SubscriptionInfo info = subscriptions.get(uuid);
        if (info != null) {
            info.setSubscription(subscription);
            wasActive = true;
            archon.getDataSource().execute("UPDATE subscriptions SET subscription = ? WHERE uuid = ?", subscription.name(), uuid);
        } else {
            info = new SubscriptionInfo(subscription, 0);
            subscriptions.put(uuid, info);

            String add = null;
            switch (subscription) {
                case ONE_WEEK:
                    add = "1 WEEK";
                    break;
                case ONE_MONTH:
                    add = "1 MONTH";
                    break;
                case THREE_MONTHS:
                    add = "3 MONTH";
                    break;
                case SIX_MONTHS:
                    add = "6 MONTH";
                    break;
            }
            archon.getDataSource().execute("INSERT IGNORE INTO subscriptions VALUES (?, ?, DATE_ADD(NOW(), INTERVAL " + add + "))", uuid, subscription.name());
        }

        Player player = archon.getPlayerByUuid(uuid);
        if (player != null) {
//            player.setSubscription(subscription);
            player.message("&3** &a(Archon Shop) &6Purchased &a" + subscription.getName().replace("s", "") + " &6subscription! &3**");
        }

        /**
         * TODO remove
         */
//        archon.sendAll(Protocol.BROADCAST.construct("&c&lArchon &8>> &6Thank you "
//                + (player != null ? player.getRank().getColor() : "&7") + name + " &6for purchasing a "
//                + subscription.getName() + " subscription &6@ &cshop.thearchon.net&6!"), ClientType.ALL_LOBBIES);

        return wasActive;
    }

    /**
     * Remove a user's subscription.
     * @param uuid uuid of player
     * @param subscription subscription to be removed
     * @return true if the user currently has a subscription and it is removed
     */
    public boolean remove(String uuid, Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("subscription");
        }

//        SubscriptionInfo info = subscriptions.remove(uuid);
//        if (info != null) {
//            Player player = archon.getPlayerByUuid(uuid);
//            if (player != null && player.getSubscription() == subscription) {
//                player.setSubscription(null);
//                player.disconnect("&cYour &6" + subscription.getName().replace("s", "") + " &csubscription has expired!\n\n&7Purchase a new subscription @ &ashop.thearchon.net");
//            }
//            archon.getDataSource().execute("DELETE FROM subscriptions WHERE uuid = ? && subscription = ?", uuid, subscription.name());
//        }
        return false;
    }

    public SubscriptionInfo getInfo(String uuid) {
        return subscriptions.get(uuid);
    }

    public boolean hasSubscription(String uuid) {
        return subscriptions.containsKey(uuid);
    }

    /*
    TODO
     */
    private void log(String uuid, Subscription subscription, long expiry) {
        archon.getDataSource().execute("INSERT INTO subscription_log VALUES (?, ?, ?)", uuid, subscription.name(), expiry);
    }
}
