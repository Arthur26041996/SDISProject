package Peer;

import Handlers.FileHandler;
import Channel.MCChannel;
import Channel.MDBChannel;
import Channel.MDRChannel;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

public class Peer extends UnicastRemoteObject implements RemoteInterface
{
    private static String name;
    private static int peerId;
    private static int protVersion;
    private static MDBChannel mdb;
    private static MDRChannel mdr;
    private static MCChannel mc;
    private static String mdbIP;
    private static String mdrIP;
    private static String mcIP;
    private static int mdbPort;
    private static int mdrPort;
    private static int mcPort;
    private static Registry rg;
    private static FileHandler fh;
    private static CustomFileSystem cfs;
    
    public Peer(String ap, int id, int version) throws RemoteException
    {
        super();
        name = ap;
        peerId = id;
        protVersion = version;
    }
    
    public static void main(String args[])
    {
        CustomFileSystem cfs = new CustomFileSystem();
        
        //Save Address and IP of the channels for further use
        mdbIP = args[0];
        mdbPort = Integer.parseInt(args[1]);
        mdrIP = args[2];
        mdrPort = Integer.parseInt(args[3]);
        mcIP = args[4];
        mcPort = Integer.parseInt(args[5]);
        
        try
        {
            Peer peer = new Peer(args[8], Integer.parseInt(args[7]), Integer.parseInt(args[6]));
           
            try
            {
                rg = LocateRegistry.createRegistry(1099);
            }
            catch(ExportException ex)
            {
                rg = LocateRegistry.getRegistry(1099);
            }
            rg.rebind(name, peer);
            
            //Initialize MDB, MDR & MC Channels
            startChannels(mdbIP, mdrIP, mcIP, mdbPort, mdrPort, mcPort);
        }
        catch(Exception ex)
        {
            System.out.println("[PEER]: Error in remoting!\nMessage: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public int getPeerId()
    {
        return peerId;
    }
    
    public static void startChannels(String mdbIP, String mdrIP, String mcIP, int mdbPort, int mdrPort, int mcPort)
    {
        try
        {
            mdb = new MDBChannel(mdbIP, mdbPort, peerId);
            mdr = new MDRChannel(mdrIP, mdrPort);
            mc = new MCChannel(mcIP, mcPort);
            mdb.run();
            mdr.run();
            mc.run();
        }
        catch (IOException ex)
        {
            System.out.println("[PEER]: Error initializing channels\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    @Override
    public void backUp(String path, int repDegree)
    {
        try
        {
            fh  = new FileHandler();
            LinkedList<Chunk> chunk = fh.splitFile(path);
            
            System.out.println("chunk list size: "+chunk.size());
            for(int i = 0; i < chunk.size(); i++)
            {
                System.out.println("size of chunk No"+chunk.get(i).ChunkNo+": "+chunk.get(i).Chunk.length);
            }
            
            //Send each chunk individually
            byte[] bufHeader;
            byte[] bufMsg;
            DatagramPacket pack;
            
            for(Chunk i : chunk)
            {
                //Message Header:
                //PUTCHUNK <VERSION> <SENDER_ID> <FILE_ID> <CHUNK_NO> <REP_DEGREE> <CRLF><CRLF>
                String msgBody = "PUTCHUNK " + 
                                protVersion + " " +
                                peerId + " " + 
                                i.FileId + " " +
                                i.ChunkNo + " " +
                                repDegree +
                                "\r\n\r\n";
                bufHeader = msgBody.getBytes();
                
                bufMsg = new byte[bufHeader.length + i.Chunk.length];
                System.arraycopy(bufHeader, 0, bufMsg, 0, bufHeader.length);
                System.arraycopy(i.Chunk, 0, bufMsg, bufHeader.length, i.Chunk.length);
                
                pack = new DatagramPacket(bufMsg, bufMsg.length, InetAddress.getByName(mdbIP), mdbPort);
                
                //while replication degree < desired
                //{
                    mdb.mcst.send(pack);
                    System.out.println("Sent chunk No"+i.ChunkNo);
                    // sleep 
                //}
            }
        }
        catch (IOException ex)
        {
            System.out.println("[PEER - BACKUP]: Error sending packet!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public String restore(String fileName)
    {
        System.out.println("RESTORE method called");
        return "";
    }

    @Override
    public void reclaim(int memoQuantity)
    {
        System.out.println("RECLAIM method called");
    }

    @Override
    public String state(int peerId)
    {
        System.out.println("STATED method called");
        return "";
    }
}
