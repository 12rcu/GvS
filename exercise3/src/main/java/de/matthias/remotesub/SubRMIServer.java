package de.matthias.remotesub;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class SubRMIServer implements SubKVStore {
    HashMap<String, Object> store = new HashMap<>();
    HashMap<Subscriber, ArrayList<String>> subscriber = new HashMap<>();
    //break out to kotlin for better streams
    Util util = new Util();
    Registry registry;

    public SubRMIServer() {
        try {
            SubKVStore stub = (SubKVStore) UnicastRemoteObject.exportObject(this, 0);

            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry();
            registry.bind("StoreSub", stub);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void writeRemote(String key, Object value) throws RemoteException {
        //notify subscribers
        util.extractSubscriberFromHashmap(subscriber, key).forEach(it -> notifySubscriberModify(it, key, value));
        //put/update the entry
        store.put(key, value);
    }

    @Override
    public Object readRemote(String key) throws RemoteException {
        util.extractSubscriberFromHashmap(subscriber, key).forEach(it -> notifySubscriberDelete(it, key));
        return store.get(key);
    }

    @Override
    public void removeRemote(String key) throws RemoteException {
        store.remove(key);
        //remove key from subscriptions EDIT: Don't do that in case the key will be put in there again -> and the client
        //wants that new value from the start
        //subscriber = util.removeKeyFromSubMap(subscriber, key);
    }

    @Override
    public void subscribe(int id, String key) throws RemoteException {
        Subscriber subStub;
        try {
            subStub = (Subscriber) registry.lookup("Client" + id);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        //don't check if a key is there -> key can be subscribed to get the initial value as well
        if(subscriber.containsKey(subStub)) {
            subscriber.get(subStub).add(key);
        } else {
            ArrayList<String> data = new ArrayList<>();
            data.add(key);
            subscriber.put(subStub, data);
        }

    }

    @Override
    public void unsubscribe(int id, String key) throws RemoteException {
        Subscriber subStub;
        try {
            subStub = (Subscriber) registry.lookup("Client" + id);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(subscriber.containsKey(subStub)) {
            subscriber.get(subStub).remove(key);
        }
    }

    private void notifySubscriberModify(Subscriber sub, String key, Object value) {
        try {
            sub.updateEntry(key, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void notifySubscriberDelete(Subscriber sub, String key) {
        try {
            sub.removeEntry(key);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        subscriber = null;
        store = null;

        System.out.println("Server destroyed");
    }
}
