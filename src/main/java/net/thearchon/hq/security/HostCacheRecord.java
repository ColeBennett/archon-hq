package net.thearchon.hq.security;

public class HostCacheRecord {

    private final String uuid;
    private boolean hostIp;
    private String org, cc, country, subdiv, postal, location;


//    country	Contains information such as the requested IP addresses country name and country code. (Professional Package Only)
//subdivision	Contains information such as the requested IP addresses subdivision name and subdivision code. (Professional Package Only)
//    city	Contains the requested IP addresses city name. (Professional Package Only)
//    postal	Contains the requested IP addresses postal and/or zip code. (Professional Package Only)
//    location

    HostCacheRecord(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isHostIp() {
        return hostIp;
    }

    public String getOrg() {
        return org;
    }

    public String getCc() {
        return cc;
    }

    public String getCountry() {
        return country;
    }

    void setHostIp(boolean hostIp) {
        this.hostIp = hostIp;
    }

    void setOrg(String org) {
        this.org = org;
    }

    void setCc(String cc) {
        this.cc = cc;
    }

    void setCountry(String country) {
        this.country = country;
    }
}
