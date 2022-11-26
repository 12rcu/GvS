package de.matthias.remotestore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteKVStore extends Remote {
    void writeRemote(String key, Object value) throws RemoteException;
    Object readRemote(String key) throws RemoteException;
    void removeRemote(String key) throws RemoteException;
}
