package de.matthias.remotestore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Test {
    public static void main(String[] args) throws RemoteException {
        LocateRegistry.createRegistry(1099);
        RMIServer server = new RMIServer();

        try {
            Registry registry = LocateRegistry.getRegistry();
            RemoteKVStore stub = (RemoteKVStore) registry.lookup("KVStore");

            //add, get & remove
            stub.writeRemote("hello", "Hello World");
            String response = (String) stub.readRemote("hello");
            System.out.println("response: " + response);
            stub.removeRemote("hello");

            //add, add, get, remove
            stub.writeRemote("weather", "nice water today!");
            stub.writeRemote("weather", "nice weather today!");
            response = (String) stub.readRemote("weather");
            System.out.println("response: " + response);
            stub.removeRemote("weather");

            //try get a removed key, expect null
            response = (String) stub.readRemote("weather");
            System.out.println("response: " + response);

        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }

        server.destroy();
    }
}
