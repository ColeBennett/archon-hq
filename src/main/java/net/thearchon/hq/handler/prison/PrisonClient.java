package net.thearchon.hq.handler.prison;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;

public class PrisonClient extends BukkitClient {
    
    public PrisonClient(Archon archon, String serverName) {
        super(archon, ServerType.PRISON, serverName);
    }  
}