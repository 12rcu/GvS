package de.matthias.remotesub;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Test {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);

            SubRMIServer server = new SubRMIServer();


            Client clientA = new Client(1);
            Client clientB = new Client(2);

            clientA.write("hello", "Hello Client B!");
            System.out.println(clientB.get("hello"));

            clientB.write("hello", "Hello Client A!");
            System.out.println(clientA.get("hello"));

            //lookup cache
            System.out.println("ClientA cache on key 'hello': '" + clientA.cache.get("hello") + "'");
            System.out.println("ClientB cache on key 'hello': '" + clientB.cache.get("hello") + "'");

            clientA.remove("hello");

            System.out.println("ClientA cache on key 'hello': '" + clientA.cache.get("hello") + "'");

            clientA.destroy();
            clientB.destroy();

            server.destroy();

            System.exit(0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
