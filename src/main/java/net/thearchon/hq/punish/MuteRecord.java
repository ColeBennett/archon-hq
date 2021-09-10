package net.thearchon.hq.punish;

public class MuteRecord {

    private final String reason;
    private final Long time;

    public MuteRecord(String reason, Long time) {
        this.reason = reason;
        this.time = time;
    }

    public String getReason() {
        return reason;
    }
    
    public Long getTime() {
        return time;
    }
}
