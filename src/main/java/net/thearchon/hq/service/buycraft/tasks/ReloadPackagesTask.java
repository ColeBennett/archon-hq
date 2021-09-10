package net.thearchon.hq.service.buycraft.tasks;

import net.thearchon.hq.service.buycraft.json.JSONArray;
import net.thearchon.hq.service.buycraft.Buycraft;
import net.thearchon.hq.service.buycraft.json.JSONException;
import net.thearchon.hq.service.buycraft.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ReloadPackagesTask implements Runnable {

    private final Buycraft buycraft;

    public ReloadPackagesTask(Buycraft buycraft) {
        this.buycraft = buycraft;
    }

    public static void call(Buycraft buycraft) {
        buycraft.addTask(new ReloadPackagesTask(buycraft));
    }

    @Override
    public void run() {
        buycraft.getPackageManager().reset();
        try {
            JSONObject categoriesResponse = buycraft.categoriesAction();
            JSONObject packagesResponse = buycraft.packagesAction();

            if (categoriesResponse == null || categoriesResponse.getInt("code") != 0 || packagesResponse == null || packagesResponse.getInt("code") != 0) {
                buycraft.getParent().getLogger().severe("No response/invalid key during package reload.");
                return;
            }
            
            JSONArray categories = categoriesResponse.getJSONArray("payload");
            JSONArray packages = packagesResponse.getJSONArray("payload");

            buycraft.getParent().getLogger().info("Categories: " + categories.length());
            buycraft.getParent().getLogger().info("Packages: " + packages.length());

            for (int i = 0; i < categories.length(); ++i) {
                JSONObject row = categories.getJSONObject(i);
                buycraft.getPackageManager().addCategory(row.isNull("id") ? 0 : row.getInt("id"), row.getString("name"));
            }

            for (int i = 0; i < packages.length(); i++) {
                if (packages.isNull(i)) {
                    continue;
                }

                JSONObject row = packages.getJSONObject(i);
                int categoryId = row.isNull("category") ? 0 : row.getInt("category");
                buycraft.getPackageManager().addPackage(categoryId, row.getInt("id"), row.get("name").toString(), row.getDouble("price"));

                List<String> keys = new ArrayList<>();
                Iterator<String> itr = row.keys();
                while (itr.hasNext()) {
                    keys.add(itr.next());
                }
                buycraft.getParent().getLogger().info(row.get("name") + ", Price: $" + row.getDouble("price") + " " + keys);
            }

            buycraft.getPackageManager().cleanCategories();
            buycraft.getParent().getLogger().info("Loaded " + packages.length() + " package(s) into the cache.");
        } catch (JSONException e) {
            buycraft.getParent().getLogger().severe("Failed to set packages due to JSON parse error.");
        } catch (Exception e) {
            buycraft.getParent().getLogger().log(Level.SEVERE, "Error while resetting packages", e);
        }
    }
}
