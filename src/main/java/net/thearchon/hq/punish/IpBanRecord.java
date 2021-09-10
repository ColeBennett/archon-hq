package net.thearchon.hq.punish;

public class IpBanRecord extends BanRecord {

    private final String ipAddr;

    public IpBanRecord(String ipAddr, String reason) {
        super(reason);

        this.ipAddr = ipAddr;
    }

    public String getIpAddress() {
        return ipAddr;
    }
}
