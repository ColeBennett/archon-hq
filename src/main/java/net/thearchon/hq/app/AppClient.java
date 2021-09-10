package net.thearchon.hq.app;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Rank;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.Client;

public class AppClient extends Client {
    
    private final String username;
    private Rank rank;
    
    public AppClient(Archon server, String username, Rank rank) {
        super(server, ServerType.APP);
        
        this.username = username;
        this.rank = rank;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public void setRank(Rank rank) {
        this.rank = rank;
    }
}