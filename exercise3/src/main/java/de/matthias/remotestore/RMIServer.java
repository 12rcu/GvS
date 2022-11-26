package de.matthias.remotestore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class RMIServer implements RemoteKVStore {
    HashMap<String, Object> store = new HashMap<>();

    public RMIServer() {
        try {
            RemoteKVStore stub = (RemoteKVStore) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("KVStore", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void writeRemote(String key, Object value) throws RemoteException {
        store.put(key, value);
    }

    @Override
    public Object readRemote(String key) throws RemoteException {
        return store.get(key);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        store.remove(key);
    }

    public void destroy() {
        store = null;
    }
}
