package net.thearchon.hq.handler.sg;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.GameClient;

public class SgClient extends GameClient {
    
    public SgClient(Archon archon, int id) {
        super(archon, ServerType.SG, "sg", id);
    }
}
