package net.thearchon.hq.security.filters;

import net.thearchon.hq.security.HostFilter;

public class HssFilter implements HostFilter {

    public boolean accept(String addr) {
        return addr.toLowerCase().contains("anchorfree");
    }
}
