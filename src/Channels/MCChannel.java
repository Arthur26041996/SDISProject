package Channels;

import Channels.Handlers.MCHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MCChannel extends Thread
{
    private int requestID;
    private final int peerID;
    private InetAddress group;
    private MulticastSocket mcst;

    public MCChannel(String ip, int port, int peerID) throws UnknownHostException, IOException
    {
        group = InetAddress.getByName(ip);
        mcst = new MulticastSocket(port);
        mcst.joinGroup(group);
        mcst.setTimeToLive(1);
        mcst.setLoopbackMode(false);
        this.peerID = peerID;
    }
    
    public void setNewRequest(int requestID)
    {
        this.requestID = requestID;
    }
    
    @Override
    public void run()
    {
        try
        {
            byte[] buff = new byte[65536];
            DatagramPacket dp = new DatagramPacket(buff, buff.length);
            
            while(true)
            {
                mcst.receive(dp);
                
                MCHandler handler = new MCHandler(dp, peerID, requestID);
                handler.start();
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MC CHANNEL]: ERROR RECEIVING PACKET");
            ex.printStackTrace();
        }
    }
}
