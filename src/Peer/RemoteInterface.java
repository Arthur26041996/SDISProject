package Peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote
{
    public void backUp(String path, int repDegree) throws RemoteException;
    public void restore(String fileName) throws RemoteException;
    public void delete(String fileName) throws RemoteException;
    public void reclaim(long memToReclaim) throws RemoteException;
    public StringBuilder state() throws RemoteException;
}
