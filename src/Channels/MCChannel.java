package Channels;

import Channels.Handlers.MCHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MCChannel extends Thread
{
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
                
                MCHandler handler = new MCHandler(dp, peerID);
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
