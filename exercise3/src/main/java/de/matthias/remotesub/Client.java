package de.matthias.remotesub;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Client implements Subscriber {
    HashMap<String, Object> cache = new HashMap<>();
    SubKVStore stub;
    Subscriber stubClient;
    private final int id;

    /**
     * Create a client that registers itself to the Registry
     *
     * @param id the identifier for the server to select the correct Client
     */
    public Client(int id) {
        this.id = id;
        try {
            Registry registry = LocateRegistry.getRegistry();
            stub = (SubKVStore) registry.lookup("StoreSub");
            stubClient = (Subscriber) UnicastRemoteObject.exportObject(this, 0);
            registry.bind("Client" + id, stubClient);

        } catch (Exception e) {
            System.out.println("Can't create Client!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntry(String key, Object value) throws RemoteException {
        cache.put(key, value);
    }

    @Override
    public void removeEntry(String key) throws RemoteException {
        cache.remove(key);
    }

    public void write(String key, Object value) {
        try {
            stub.writeRemote(key, value);
            stub.subscribe(this.id, key);
            cache.put(key, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void remove(String key) {
        try {
            stub.removeRemote(key);
            stub.unsubscribe(this.id, key);
            cache.remove(key);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        if(cache.containsKey(key)) {
            return cache.get(key);
        } else {
            try {
                return stub.readRemote(key);
            } catch (RemoteException e) {
                return null;
            }
        }
    }

    public void destroy() {
        cache = null;
        stub = null;

        System.out.println("Client destroyed");
    }
}
