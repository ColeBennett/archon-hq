package net.thearchon.hq.service;

import java.util.Set;

public interface Service<T extends Listener> {

    /**
     * Service has initialized.
     */
    void initialize();

    /**
     * Service has been shut down.
     */
    void shutdown();

    /**
     * @return set of registered listeners
     */
    Set<T> getListeners();

    /**
     * Register a listener to this service.
     * @param listener listener to be registered
     */
    void addListener(T listener);

    /**
     * Unregister a listener from this service.
     * @param listener listener to be unregistered
     */
    void removeListener(T listener);

    /**
     * @param listener listener to check
     * @return true if this service contains a matching registered listener
     */
    boolean hasListener(T listener);
}
