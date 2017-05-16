package Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MDBChannel implements Runnable
{
    private String IP;
    private int PORT;
    private InetAddress group;
    private MulticastSocket mcst;
    
    public MDBChannel(String IP, int PORT) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
    }

    @Override
    public void run()
    {
        try
        {
            mcst.joinGroup(group);
            
            int chunkSize = 64000;
            byte[] buff = new byte[chunkSize];
            DatagramPacket dp = new DatagramPacket(buff, buff.length);
            
            while(true)
            {
                mcst.receive(dp);
                
                String message = new String(dp.getData(), 0, dp.getLength());
                System.out.println(message);
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MDBCHANNEL]: Error receiving package!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
