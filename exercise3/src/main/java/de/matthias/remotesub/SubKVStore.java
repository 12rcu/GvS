package de.matthias.remotesub;

import de.matthias.remotestore.RemoteKVStore;

import java.rmi.RemoteException;

public interface SubKVStore extends RemoteKVStore {
    /**
     * subscribe to a key to get updates when the value changes, you can subscribe to multiple keys
     *
     * @param id the subscriber to receive updates
     * @param key the key to subscribe to
     */
    void subscribe(int id, String key) throws RemoteException;

    /**
     * unsubscribe from a key
     *
     * @param id the subscriber to revoke the subscription
     * @param key the key that should be removed from the subscriber list
     */
    void unsubscribe(int id, String key) throws RemoteException;
}
