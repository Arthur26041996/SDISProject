package Channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MDRChannel extends Thread
{
    private final InetAddress group;
    private final int peerID;
    private MulticastSocket mcst;

    public MDRChannel(String ip, int port, int peerID) throws UnknownHostException, IOException
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
                //(new Thread(new RestoreHandler(dp, Peer.Peer.getPeerId()))).start();
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MDB CHANNEL]: ERROR RECEIVING PACKET");
            ex.printStackTrace();
        }
    }
    
}
