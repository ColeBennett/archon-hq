package net.thearchon.hq.service.buycraft.tasks;

import net.thearchon.hq.service.buycraft.Buycraft;
import net.thearchon.hq.service.buycraft.json.JSONObject;

public class AuthenticateTask implements Runnable {

    private final Buycraft buycraft;

    public AuthenticateTask(Buycraft buycraft) {
        this.buycraft = buycraft;
    }

    public static void call(Buycraft buycraft) {
        buycraft.addTask(new AuthenticateTask(buycraft));
    }

    @Override
    public void run() {
        try {
            JSONObject apiResponse = buycraft.authenticateAction();
            buycraft.setAuthenticated(false);
            if (apiResponse != null) {
                buycraft.setAuthenticatedCode(apiResponse.getInt("code"));
                if (apiResponse.getInt("code") == 0) {
                    JSONObject payload = apiResponse.getJSONObject("payload");

                    buycraft.setServerId(payload.getInt("serverId"));
                    buycraft.setServerCurrency(payload.getString("serverCurrency"));
                    buycraft.setServerStore(payload.getString("serverStore"));
                    buycraft.setPendingPlayerCheckerInterval(payload.getInt("updateUsernameInterval"));
                    buycraft.setAuthenticated(true);

                    boolean requiresOnlineMode = payload.getBoolean("onlineMode");
                    Buycraft.onlineMode = requiresOnlineMode;

                    buycraft.getParent().getLogger().info("Requires online mode = " + requiresOnlineMode);
                    buycraft.getParent().getLogger().info("Authenticated with the specified Secret key.");

                    ReloadPackagesTask.call(buycraft);
                } else if (apiResponse.getInt("code") == 101) {
                    buycraft.getParent().getLogger().severe("The specified Secret key could not be found.");
                    buycraft.getParent().getLogger().severe("Type /buycraft for further advice & help.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
