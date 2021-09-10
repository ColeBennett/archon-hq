package net.thearchon.hq;

public enum ServerRegion {

    NA("North America", "pvp.thearchon.net"),
    EU("Europe", "eu.thearchon.net");

    private final String name, ip;

    ServerRegion(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }
}
