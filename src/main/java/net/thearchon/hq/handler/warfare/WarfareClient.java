package net.thearchon.hq.handler.warfare;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.GameClient;

public class WarfareClient extends GameClient {
    
    public WarfareClient(Archon archon, int id) {
        super(archon, ServerType.WARFARE, "warfare", id);
    }
}
