package net.thearchon.hq.service;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.PlayerInfo;
import net.thearchon.hq.service.buycraft.Buycraft;
import net.thearchon.hq.service.votifier.VoteReceiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ServiceManager {

    private final Archon archon;
    private final List<Service> services = new ArrayList<>();

    public ServiceManager(Archon archon) {
        this.archon = archon;

        /*
         * Votifier
         */
        VoteReceiver voteReceiver = new VoteReceiver();
        voteReceiver.addListener(vote -> {
            PlayerInfo info = archon.getPlayerInfo(vote.getUsername());
            if (info != null) {
                archon.getDataSource().execute("INSERT INTO votes (time, service, player, ip_address) VALUES(NOW(), ?, ?, ?);", vote.getServiceName(), info.getId(), vote.getAddress());
                archon.getDataSource().execute("UPDATE players SET votes = votes + 1, unclaimed_votes = unclaimed_votes + 1 WHERE id = ?;", info.getId());
                Player player = archon.getPlayerByName(info.getName());
                if (player != null) {
                    player.setUnclaimedVotes(player.getUnclaimedVotes() + 1);
                    player.message("&dThank you for voting! You can now &e/claim &dyour reward!");
                }
            }
        });
        services.add(voteReceiver);

        /*
         * Buycraft
         */
        Buycraft buycraft = new Buycraft(archon);
        buycraft.addListener(archon.getPaymentLog());
        services.add(buycraft);
    }

    public void initServices() {
        for (Service service : services) {
            try {
                service.initialize();
            } catch (Throwable t) {
                archon.getLogger().log(Level.SEVERE, "Failed to initialize service: " + service.getClass().getSimpleName(), t);
            }
        }
    }

    public void shutdownServices() {
        Iterator<Service> itr = services.iterator();
        while (itr.hasNext()) {
            Service service = itr.next();
            try {
                service.shutdown();
            } catch (Throwable t) {
                archon.getLogger().log(Level.SEVERE, "Failed to shutdown service: " + service.getClass().getSimpleName(), t);
            }
            itr.remove();
        }
    }

    public List<Service> getServices() {
        return services;
    }
}
