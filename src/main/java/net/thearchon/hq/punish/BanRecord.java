package net.thearchon.hq.punish;

public class BanRecord {
    
    private final String reason;

    public BanRecord(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}
