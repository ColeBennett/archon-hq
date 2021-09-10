package net.thearchon.hq.service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractService<T extends Listener> implements Service<T> {

    private final Set<T> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public Set<T> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(T listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(T listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean hasListener(T listener) {
        return listeners.contains(listener);
    }
}
