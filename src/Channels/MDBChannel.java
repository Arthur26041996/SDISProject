package Channels;

import Channels.Handlers.MDBHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MDBChannel extends Thread
{
    private final InetAddress group;
    private final int peerID;
    public MulticastSocket mcst;
    
    
    public MDBChannel(String IP, int PORT, int peerID) throws UnknownHostException, IOException
    {  
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
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
                
                MDBHandler handler = new MDBHandler(dp, peerID);
                handler.start();
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MDB CHANNEL]: ERROR RECEIVING PACKET");
            ex.printStackTrace();
        }
    }
}
