package net.thearchon.hq;

import net.thearchon.hq.util.io.JsonConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Settings extends JsonConfig {

    public static final File FILE = new File("settings.json");

    public Settings() {
        super(FILE);
        setSaveOnWrite(true);
    }

    public int getNetworkSlots() {
        return getInt("slots.network");
    }

    public int getLobbySlots() {
        return getInt("slots.lobby");
    }

    public String getMotd() {
        return getString("motd.na");
    }

    public String getMotd(ServerRegion region) {
        return getMotd();
    }

    public boolean getMaintenanceMode() {
        return getBoolean("maintenance.enabled");
    }

    public String getMaintenanceMotd() {
        return getString("maintenance.motd");
    }

    public String getMysqlHost() {
        return getString("mysql.host");
    }

    public int getMysqlPort() {
        return getInt("mysql.port");
    }

    public String getMysqlDatabase() {
        return getString("mysql.database");
    }

    public String getMysqlUsername() {
        return getString("mysql.username");
    }

    public String getMysqlPassword() {
        return getString("mysql.password");
    }

    public String getMongoDbHost() {
        return getString("mongodb.host");
    }

    public String getMongoDbUsername() {
        return getString("mongodb.username");
    }

    public String getMongoDbPassword() {
        return getString("mongodb.password");
    }

    public List<String> getDisabledCommands() {
        return getStringList("disabledCommands");
    }

    public Map<String, String> getBlockedWords() {
        return getMap("chat.blockedWords");
    }

    public List<String> getChangelog() {
        return getStringList("changelog");
    }

    public int getCapWarnsBeforeKick() {
        return getInt("capWarnsBeforeKick");
    }

    public int getSwearWarnsBeforeTempban() {
        return getInt("swearWarnsBeforeTempban");
    }

    public double getSmpMaxOverflowPerc() {
        return getFloat("smpMaxOverflowPerc");
    }

    /**
     * Set Methods
     */

    public void setSlots(int slots) {
        set("slots.network", slots);
    }

    public void setLobbySlots(int lobbySlots) {
        set("slots.lobby", lobbySlots);
    }

    public void setMotd(String motd) {
        set("motd.na", motd);
    }

    public void setMaintenanceMode(boolean enabled) {
        set("maintenance.enabled", enabled);
    }

    public void setMaintenanceMotd(String maintenanceMotd) {
        set("maintenance.motd", maintenanceMotd);
    }
}
