package Peer;

import Handlers.FileHandler;
import Channel.MCChannel;
import Channel.MDBChannel;
import Channel.MDRChannel;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer extends UnicastRemoteObject implements RemoteInterface
{
    private static String name;
    private static int peerId;
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
    private static FileHandler fh;
    public static RepDegRecord rdr;
    
    public Peer(String ap, int id, float version) throws RemoteException
    {
        super();
        name = ap;
        peerId = id;
        protVersion = version;
    }
    
    public static void main(String args[])
    {   
        //Save Address and IP of the channels for further use
        mdbIP = args[0];
        mdbPort = Integer.parseInt(args[1]);
        mdrIP = args[2];
        mdrPort = Integer.parseInt(args[3]);
        mcIP = args[4];
        mcPort = Integer.parseInt(args[5]);
        
        try
        {
            Peer peer = new Peer(args[8], Integer.parseInt(args[7]), Float.parseFloat(args[6]));
           
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
        catch(NumberFormatException | RemoteException ex)
        {
            System.out.println("[PEER - MAIN]: Error in remoting or starting channels!\nMessage: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static int getPeerId()
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
            (new Thread(mdb)).start();
            (new Thread(mdr)).start();
            (new Thread(mc)).start();
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
                String msgHeader = "PUTCHUNK " + 
                                protVersion + " " +
                                peerId + " " + 
                                i.FileId + " " +
                                i.ChunkNo + " " +
                                repDegree +
                                "\r\n\r\n";
                bufHeader = msgHeader.getBytes();
                
                rdr = new RepDegRecord();
                rdr.addRecord(i.FileId, repDegree, 0);
                
                //Concatenates the bytes from the message Header with the bytes from the Chunk
                //in a new byte array.
                bufMsg = new byte[bufHeader.length + i.Chunk.length];
                System.arraycopy(bufHeader, 0, bufMsg, 0, bufHeader.length);
                System.arraycopy(i.Chunk, 0, bufMsg, bufHeader.length, i.Chunk.length);
                
                pack = new DatagramPacket(bufMsg, bufMsg.length, InetAddress.getByName(mdbIP), mdbPort);
                
                int msToSleep = 1000; //Set the time this thread should wait for responses
                                      //before sending the chunk again.
                int count = 0;
                
                System.out.println("Sending chunk No"+i.ChunkNo);
                while((rdr.getActualRepDegree(i.FileId) < repDegree) && (count < 5))
                {
                    mdb.mcst.send(pack);
                    System.out.println("Actual replication Degree: "+rdr.getActualRepDegree(i.FileId));
                    Thread.sleep(msToSleep);
                    msToSleep *= 2;
                    count++;
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("[PEER - BACKUP]: Error sending packet!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
        catch (InterruptedException ex)
        {
            System.out.println("[PEER - BACKUP]: Error in Thread.sleep!\nMESSAGE: "+ex.getMessage());
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
    public void delete(String fileName)
    {
        fh = new FileHandler();
        String fileID = fh.encoding(fileName);
        String message = "DELETE "+
                        protVersion + " " +
                        peerId + " " +
                        fileID +
                        "\r\n\r\n";
        
        try
        {
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(mcIP), mcPort);
            mc.mcst.send(dp);
        }
        catch (IOException ex)
        {
            System.out.println("[PEER - DELETE]: Error sending packet!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
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
