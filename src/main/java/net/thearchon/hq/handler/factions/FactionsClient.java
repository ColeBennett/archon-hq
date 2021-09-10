package net.thearchon.hq.handler.factions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ChatColor;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.handler.ServerJoinQueue;
import net.thearchon.hq.util.Util;

import java.util.Map;
import java.util.Map.Entry;

public class FactionsClient extends BukkitClient {

    private ChatColor color;
    private String youtuber;
    private Map<Integer, String> maps;

    private ServerJoinQueue queue;

    public FactionsClient(Archon archon, String serverName, ChatColor color,
            String youtuber, Map<Integer, String> maps) {
        super(archon, ServerType.FACTIONS, serverName);

        this.color = color;
        this.youtuber = youtuber;
        this.maps = maps;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getYoutuber() {
        return youtuber;
    }

    public Map<Integer, String> getMaps() {
        return maps;
    }

    public Entry<Integer, String> getCurrentMap() {
        Entry<Integer, String> max = null;
        for (Entry<Integer, String> entry : maps.entrySet()) {
            if (max == null || entry.getKey() > max.getKey()) {
                max = entry;
            }
        }
        return max;
    }

    public ServerJoinQueue getQueue() {
        return queue;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void setYoutuber(String youtuber) {
        this.youtuber = youtuber;
    }

    public void setMaps(Map<Integer, String> maps) {
        this.maps = maps;
    }

    public void setQueue(ServerJoinQueue queue) {
        this.queue = queue;
    }

    @Override
    public String getDisplayName() {
        return Util.upperLower(getServerName().replace("faction", ""));
    }
}