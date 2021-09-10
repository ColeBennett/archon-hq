package net.thearchon.hq.service.buycraft;

import net.thearchon.hq.Archon;
import net.thearchon.hq.service.AbstractService;
import net.thearchon.hq.service.buycraft.packages.PackageManager;
import net.thearchon.hq.service.buycraft.tasks.AuthenticateTask;
import net.thearchon.hq.service.buycraft.tasks.CommandFetchTask;
import net.thearchon.hq.service.buycraft.tasks.PendingPlayerCheckerTask;
import net.thearchon.hq.service.buycraft.json.JSONArray;
import net.thearchon.hq.service.buycraft.json.JSONException;
import net.thearchon.hq.service.buycraft.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Buycraft extends AbstractService<CommandListener> {

    private final Archon archon;

    private final String apiUrl = "http://api.buycraft.net/v4";
    private final String apiKey = "541bc290d7e6e572f9fb02bcf594e6acb6140ab8";

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private final boolean debug = true;
    public static boolean onlineMode;

    private int serverId;
    private String serverCurrency = "";
    private String serverStore = "";

    private PackageManager packageManager;
    private CommandFetchTask commandFetchTask;
    private PendingPlayerCheckerTask pendingPlayerCheckerTask;
    private ScheduledFuture<?> pendingPlayerCheckerTaskExecutor;

    private boolean authenticated;
    private int authenticatedCode = 1;

    public Buycraft(Archon archon) {
        this.archon = archon;
    }

    @Override
    public void initialize() {
//        if (true) return;

        packageManager = new PackageManager();
        commandFetchTask = new CommandFetchTask(this);
        pendingPlayerCheckerTask = new PendingPlayerCheckerTask(this);

        AuthenticateTask.call(this);
    }

    @Override
    public void shutdown() {
        commandFetchTask.deleteCommands();
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public JSONObject authenticateAction() {
        Map<String, String> apiCallParams = new HashMap<>(1);

        apiCallParams.put("action", "info");

//        apiCallParams.put("serverPort", "25565");
//        apiCallParams.put("onlineMode", String.valueOf(Plugin.onlineMode));
//        apiCallParams.put("playersMax", "0");
//        apiCallParams.put("version", "6.7");

        return call(apiCallParams);
    }

    public JSONObject categoriesAction() {
        Map<String, String> apiCallParams = new HashMap<>(1);

        apiCallParams.put("action", "categories");

        return call(apiCallParams);
    }

    public JSONObject packagesAction() {
        Map<String, String> apiCallParams = new HashMap<>(1);

        apiCallParams.put("action", "packages");

        return call(apiCallParams);
    }

    public JSONObject fetchPendingPlayers(boolean useUuids) {
        Map<String, String> apiCallParams = new HashMap<>(2);

        apiCallParams.put("action", "pendingUsers");
        apiCallParams.put("userType", useUuids ? "uuid" : "ign");

        return call(apiCallParams);
    }

    public JSONObject fetchPlayerCommands(JSONArray players, boolean offlineCommands, boolean useUuids) {
        Map<String, String> apiCallParams = new HashMap<>(6);

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "lookup");

        apiCallParams.put("users", players.toString());
        apiCallParams.put("userType", useUuids ? "uuid" : "ign");
        apiCallParams.put("offlineCommands", String.valueOf(offlineCommands));
        apiCallParams.put("offlineCommandLimit", "150");

        return call(apiCallParams);
    }

    public JSONObject commandsDeleteAction(String commandsToDelete) {
        Map<String, String> apiCallParams = new HashMap<>(3);

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "removeId");
        apiCallParams.put("commands", commandsToDelete);

        return call(apiCallParams);
    }

    private JSONObject call(Map<String, String> apiCallParams) {
        apiCallParams.put("secret", apiKey);
        apiCallParams.put("playersOnline", "0");

        archon.getLogger().info("\nCalling - action: " + apiCallParams.get("action") + ", do: " + apiCallParams.get("do"));
        String response = httpRequest(apiUrl + generateUrlQuery(apiCallParams));
        if (response != null) {
            try {
                return new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String httpRequest(String url) {
        try {
            if (debug) {
                archon.getLogger().info("---------------------------------------------------");
                archon.getLogger().info("Request URL: " + url);
            }

            URL conn = new URL(url);
            HttpURLConnection yc = (HttpURLConnection) conn.openConnection();

            yc.setRequestMethod("GET");
            yc.setConnectTimeout(15000);
            yc.setReadTimeout(15000);
            yc.setInstanceFollowRedirects(false);
            yc.setAllowUserInteraction(false);

            StringBuilder buf = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                buf.append(inputLine);
            }
            String content = buf.toString();
            in.close();

            if (debug) {
                archon.getLogger().info("Response: " + content);
                archon.getLogger().info("---------------------------------------------------\n");
            }
            return content;
        } catch (ConnectException e) {
            archon.getLogger().severe("HTTP request failed due to connection error.");
        } catch (SocketTimeoutException e) {
            archon.getLogger().severe("HTTP request failed due to timeout error.");
        } catch (FileNotFoundException e) {
            archon.getLogger().severe("HTTP request failed due to file not found.");
        } catch (UnknownHostException e) {
            archon.getLogger().severe("HTTP request failed due to unknown host.");
        } catch (IOException e) {
            archon.getLogger().severe(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateUrlQuery(Map<String, String> values) {
        StringBuilder buf = new StringBuilder();
        buf.append('?');
        for (Entry<String, String> entry : values.entrySet()) {
            if (buf.length() > 1) {
                buf.append('&');
            }
            buf.append(String.format("%s=%s",
                    entry.getKey(),
                    entry.getValue()
            ));
        }
        return buf.toString();
    }

    public Archon getParent() {
        return archon;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public void addTask(Runnable task) {
        executor.submit(task);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean value) {
        authenticated = value;
    }

    public void setAuthenticatedCode(Integer value) {
        authenticatedCode = value;
    }

    public Integer getAuthenticatedCode() {
        return authenticatedCode;
    }

    public void setServerId(Integer value) {
        serverId = value;
    }

    public void setServerCurrency(String value) {
        serverCurrency = value;
    }

    public void setServerStore(String value) {
        serverStore = value;
    }

    public void setPendingPlayerCheckerInterval(int interval) {
        if (pendingPlayerCheckerTaskExecutor != null) {
            pendingPlayerCheckerTaskExecutor.cancel(true);
            pendingPlayerCheckerTaskExecutor = null;
        }
        pendingPlayerCheckerTaskExecutor = executor.scheduleAtFixedRate(
                pendingPlayerCheckerTask::call, 0, interval, TimeUnit.SECONDS);
    }

    public Integer getServerId() {
        return serverId;
    }

    public String getServerStore() {
        return serverStore;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public CommandFetchTask getCommandFetchTask() {
        return commandFetchTask;
    }

    public String getServerCurrency() {
        return serverCurrency;
    }

    public static String addDashesToUUID(String s) {
        return new StringBuilder()
                .append(s, 0, 8).append('-')
                .append(s, 8, 12).append('-')
                .append(s, 12, 16).append('-')
                .append(s, 16, 20).append('-')
                .append(s, 20, 32).toString();
    }

    public static String uuidToString(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }
}
