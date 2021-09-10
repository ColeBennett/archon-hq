package net.thearchon.hq.security.filters;

import net.thearchon.hq.security.HostFilter;

public class XioaxFilter implements HostFilter {

    @Override
    public boolean accept(String addr) {
        return false;
    }
}
