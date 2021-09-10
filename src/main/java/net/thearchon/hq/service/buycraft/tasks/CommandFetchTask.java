package net.thearchon.hq.service.buycraft.tasks;

import net.thearchon.hq.service.buycraft.CommandListener;
import net.thearchon.hq.service.buycraft.json.JSONArray;
import net.thearchon.hq.service.buycraft.Buycraft;
import net.thearchon.hq.service.buycraft.PackageCommand;
import net.thearchon.hq.service.buycraft.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class CommandFetchTask {

    private final Buycraft buycraft;

    private final Queue<PackageCommand> queue = new ConcurrentLinkedQueue<>();
    private final Set<Integer> commandsToDelete = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public CommandFetchTask(Buycraft buycraft) {
        this.buycraft = buycraft;
    }

    public void fetchAndExecute(boolean offlineCommands, String[] players) {
        try {
            if (!buycraft.isAuthenticated()) {
                return;
            }
            boolean useUuids = Buycraft.onlineMode;

            String[] playerKeys = null;
            if (players.length > 0) {
                ArrayList<String> tmpPlayerKeys = new ArrayList<>(players.length);
                for (String player : players) {
                    tmpPlayerKeys.add(player);
//                    tmpPlayerKeys.add(useUuids ? UuidUtil.uuidToString(player.getUniqueId()) : player);
                }
                playerKeys = tmpPlayerKeys.toArray(new String[tmpPlayerKeys.size()]);
            } else {
                playerKeys = new String[0];
            }

            JSONObject apiResponse = buycraft.fetchPlayerCommands(new JSONArray(playerKeys), offlineCommands, useUuids);
            if (apiResponse == null || apiResponse.getInt("code") != 0) {
                buycraft.getParent().getLogger().severe("No response/invalid key during package check.");
                return;
            }

            JSONObject apiPayload = apiResponse.getJSONObject("payload");
            JSONArray commandsPayload = apiPayload.getJSONArray("commands");

            for (int i = 0; i < commandsPayload.length(); i++) {
                JSONObject row = commandsPayload.getJSONObject(i);

                int commandId = row.getInt("id");
                String username = row.getString("ign");

                String uuid = null;
                if (row.has("uuid") && row.getString("uuid").length() > 0) {
                    uuid = Buycraft.addDashesToUUID(row.getString("uuid"));
                }

                //boolean requireOnline = row.getBoolean("requireOnline");
                String command = row.getString("command");
//                int delay = row.getInt("delay");

                queue.add(new PackageCommand(commandId, uuid, username, command));
            }
            execute();
            buycraft.getParent().getLogger().info("Package checker successfully executed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute() {
        buycraft.getParent().runTask(() -> {
            PackageCommand command;
            while ((command = queue.poll()) != null) {
                buycraft.getParent().getLogger().info("Executing command '" + command.getCommand() + "' on behalf of user '" + command.getUsername() + "'.");
                for (CommandListener listener : buycraft.getListeners()) {
                    try {
                        listener.commandReceived(command);
                    } catch (Throwable t) {
                        buycraft.getParent().getLogger().log(Level.WARNING, "Failed to handle package command: " + command.getCommand(), t);
                    }
                }
                commandsToDelete.add(command.getId());
            }

            buycraft.getExecutor().execute(this::deleteCommands);
        });
    }

    public void deleteCommands() {
        if (!commandsToDelete.isEmpty()) {
            try {
                Integer[] ids = commandsToDelete.toArray(new Integer[commandsToDelete.size()]);
                if (buycraft.commandsDeleteAction(new JSONArray(ids).toString()) == null) {
                    buycraft.getParent().getLogger().log(Level.SEVERE, "No response found, cancelling deletion.");
                    return;
                }
                commandsToDelete.clear();
            } catch (Throwable t) {
                buycraft.getParent().getLogger().log(Level.SEVERE, "Error occured when deleting commands from the API", t);
            }
        }
    }
}
