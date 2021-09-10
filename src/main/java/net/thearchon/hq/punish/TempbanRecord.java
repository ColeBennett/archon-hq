package net.thearchon.hq.punish;

public class TempbanRecord extends BanRecord {
    
    private final long time;
    
    public TempbanRecord(String reason, long time) {
        super(reason);
        
        this.time = time;
    }
    
    public long getTime() {
        return time;
    }
}
