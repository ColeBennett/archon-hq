package net.thearchon.hq.data;

import net.thearchon.hq.util.io.JsonUtil;

import java.io.File;

public class LocalCache {

    public static final File FILE = new File("data", "cache.json");
    
    private int record;
    private int currentMostOnline;
    private int currentNewPlayers;

    public int getRecord() {
        return record;
    }

    public int getCurrentMostOnline() {
        return currentMostOnline;
    }

    public int getCurrentNewPlayers() {
        return currentNewPlayers;
    }

    public void setRecord(int record) {
        this.record = record;
        save();
    }

    public void setCurrentMostOnline(int mostOnline) {
        currentMostOnline = mostOnline;
        save();
    }

    public void setCurrentNewPlayers(int newPlayers) {
        currentNewPlayers = newPlayers;
        save();
    }

    public void save() {
        JsonUtil.save(FILE, this);
    }
}
