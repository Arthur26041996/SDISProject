package Peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote
{
    public void backUp(String path, int repDegree) throws RemoteException;
    public String restore(String fileName) throws RemoteException;
    public void reclaim(int memoQuantity) throws RemoteException;
    public String state(int peerId) throws RemoteException;
}
