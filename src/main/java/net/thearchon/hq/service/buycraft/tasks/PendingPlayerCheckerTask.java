package net.thearchon.hq.service.buycraft.tasks;

import net.thearchon.hq.service.buycraft.Buycraft;
import net.thearchon.hq.service.buycraft.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class PendingPlayerCheckerTask implements Runnable {

    private final Buycraft buycraft;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PendingPlayerCheckerTask(Buycraft buycraft) {
        this.buycraft = buycraft;
    }

    @Override
    public void run() {
        try {
            if (!buycraft.isAuthenticated()) {
                return;
            }

            JSONObject apiResponse = buycraft.fetchPendingPlayers(Buycraft.onlineMode);
            if (apiResponse == null || apiResponse.getInt("code") != 0) {
                buycraft.getParent().getLogger().severe("No response/invalid key during pending players check.");
                return;
            }

            JSONObject apiPayload = apiResponse.getJSONObject("payload");
            //JSONArray pendingPlayers = apiPayload.getJSONArray("pendingPlayers");
            boolean offlineCommands = apiPayload.getBoolean("offlineCommands");

            /*
            ArrayList<String> onlinePendingPlayers = null;
            // No point in this if there are no pending players
            if (pendingPlayers.length() > 0) {
                onlinePendingPlayers = new ArrayList<String>();

                // Iterate through each pending player
                for (int i = 0; i < pendingPlayers.length(); ++i) {
                    String playerKey = pendingPlayers.getString(i);
                    if (!buycraft.onlineMode) {
                        // Player names should be in lowercase
                        playerKey = playerKey.toLowerCase();
                    }
                    onlinePendingPlayers.add(playerKey);
                }
            }
            */

            // Check if we need to run the command checker
            if (offlineCommands /*|| (onlinePendingPlayers != null && !onlinePendingPlayers.isEmpty())*/) {
                // Create the array of players which will need commands to be fetched now
                //String[] players = onlinePendingPlayers != null ? onlinePendingPlayers.toArray(new String[onlinePendingPlayers.size()]) : new String[0];

                buycraft.getCommandFetchTask().fetchAndExecute(offlineCommands, new String[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            running.set(false);
        }
    }

    public void call() {
        if (running.compareAndSet(false, true)) {
            buycraft.addTask(this);
        }
    }
}
