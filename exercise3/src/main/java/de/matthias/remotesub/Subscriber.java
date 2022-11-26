package de.matthias.remotesub;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Subscriber extends Remote {
    void updateEntry(String key, Object value) throws RemoteException;
    void removeEntry(String key) throws RemoteException;
}
