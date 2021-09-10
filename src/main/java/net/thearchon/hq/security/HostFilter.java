package net.thearchon.hq.security;

public interface HostFilter {

    boolean accept(String addr);
}
