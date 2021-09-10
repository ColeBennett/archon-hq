package net.thearchon.hq.handler.skywars;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.GameClient;

public class SwClient extends GameClient {
    
    public SwClient(Archon archon, int id) {
        super(archon, ServerType.SKYWARS, "skywars", id);
    }
}
