package net.thearchon.hq.client;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;

public class LobbyClient extends IdBukkitClient {
    
    public LobbyClient(Archon archon, int id) {
        super(archon, ServerType.LOBBY, "lobby", id);
    }
    
    @Override
    public String getServerName() {
        return getId() == 0 ? "setup" : super.getServerName();
    }
}
