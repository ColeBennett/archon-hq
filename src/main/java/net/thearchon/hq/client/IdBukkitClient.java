package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;

public abstract class IdBukkitClient extends BukkitClient {
    
    private final int id;

    public IdBukkitClient(Archon archon, ServerType type,
            String prefix, int id) {
        super(archon, type, prefix + id);
        
        this.id = id;
    }
    
    public int getId() {
        return id;
    }  
}
