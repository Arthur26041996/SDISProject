package Peer;

import Channels.MCChannel;
import Channels.MDBChannel;
import Channels.MDRChannel;
import Handlers.ReclaimHandler;
import Senders.MCSender;
import Senders.MDBSender;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

public class Peer extends UnicastRemoteObject implements RemoteInterface
{
    private static String name;
    private static int peerID;
    private static float protVersion;
    private static MDBChannel mdb;
    private static MDRChannel mdr;
    public static MCChannel mc;
    private static String mdbIP;
    private static String mdrIP;
    private static String mcIP;
    private static int mdbPort;
    private static int mdrPort;
    private static int mcPort;
    private static Registry rg;
    public static State state;
    public static RepDegree rd;
    
    public Peer(String ap, int id, float version, long totalMem) throws RemoteException
    {
        super();
        name = ap;
        peerID = id;
        protVersion = version;
        state = new State(peerID, (totalMem));
        rd = new RepDegree();
    }
    
    public static void main(String args[])
    {
        mdbIP = args[0];
        mdbPort = Integer.parseInt(args[1]);
        mdrIP = args[2];
        mdrPort = Integer.parseInt(args[3]);
        mcIP = args[4];
        mcPort = Integer.parseInt(args[5]);
        
        try
        {
            Peer peer = new Peer(args[8], Integer.parseInt(args[7]), Float.parseFloat(args[6]), Long.parseLong(args[9])*1000);
           
            try
            {
                rg = LocateRegistry.createRegistry(3050);
            }
            catch(ExportException ex)
            {
                rg = LocateRegistry.getRegistry(3050);
            }
            rg.rebind(name, peer);
            
            startChannels();
            
            System.out.println("\n[PEER]: SUCCESSFULLY INITIALIZED PEER"
                              +"\n\tPEER ID: "+peerID
                              +"\n\tPEER AP: "+name
                              +"\n\tMDB ADDRESS: "+mdbIP+":"+mdbPort
                              +"\n\tMDR ADDRESS: "+mdrIP+":"+mdrPort
                              +"\n\tMC ADDRESS: "+mcIP+":"+mcPort
                              +"\r\n\r\n");
        }
        catch(RemoteException ex)
        {
            System.out.println("[PEER]: ERROR IN REMOTING");
            ex.printStackTrace();
        }
    }
    
    public static int getPeerID()
    {
        return peerID;
    }

    public static String getMdbIP()
    {
        return mdbIP;
    }

    public static String getMdrIP()
    {
        return mdrIP;
    }

    public static String getMcIP()
    {
        return mcIP;
    }

    public static int getMdbPort()
    {
        return mdbPort;
    }

    public static int getMdrPort()
    {
        return mdrPort;
    }
    
    public static int getMcPort()
    {
        return mcPort;
    }

    public static float getProtVersion()
    {
        return protVersion;
    }

    public static void setProtVersion(float protVersion)
    {
        Peer.protVersion = protVersion;
    }
    
    
    
    public static void startChannels()
    {
        try
        {
            mdb = new MDBChannel(mdbIP, mdbPort, peerID);
            mdr = new MDRChannel(mdrIP, mdrPort, peerID);
            mc = new MCChannel(mcIP, mcPort, peerID);
            mdb.start();
            mdr.start();
            mc.start();
        }
        catch (IOException ex)
        {
            System.out.println("[PEER]: Error initializing channels\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    @Override
    public void backUp(String file, int repDegree)
    {
        try
        {
            MDBSender sender = new MDBSender(mdbIP, mdbPort, protVersion, peerID, file, repDegree);
            sender.start();
        }
        catch (SocketException ex)
        {
            System.out.println("[PEER - BACKUP]: FAILED ATTEMPTING TO OPEN SOCKET");
            ex.printStackTrace();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("[PEER - BACKUP]: UNKNOWN HOST");
            ex.printStackTrace();
        }
    }

    @Override
    public void restore(String fileName)
    {
        try
        {
            MCSender sender = new MCSender(mcIP, mcPort, protVersion, peerID, "RESTORE");
            sender.setFile(fileName);
            sender.start();
        }
        catch (SocketException ex)
        {
            System.out.println("[PEER - RESTORE]: FAILED ATTEMPTING TO OPEN SOCKET");
            ex.printStackTrace();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("[PEER - RESTORE]: UNKNOWN HOST");
            ex.printStackTrace();
        }
    }
    
    @Override
    public void delete(String fileName)
    {
        try
        {
            MCSender sender = new MCSender(mcIP, mcPort, protVersion, peerID, "DELETE");
            sender.setFile(fileName);
            sender.start();
        }
        catch (SocketException ex)
        {
            System.out.println("[PEER - DELETE]: FAILED ATTEMPTING TO OPEN SOCKET");
            ex.printStackTrace();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("[PEER - DELETE]: UNKNOWN HOST");
            ex.printStackTrace();
        }
    }

    @Override
    public void reclaim(long memToReclaim)
    {
        ReclaimHandler handler = new ReclaimHandler(memToReclaim);
        handler.start();
    }

    @Override
    public StringBuilder state()
    {
        return state.print();
    }
}
