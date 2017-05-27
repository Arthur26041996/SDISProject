package Channel;

import Handlers.BackupHandler;
import Peer.CustomFileSystem;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MDBChannel implements Runnable
{
    private String IP;
    private int PORT;
    private int peerID;
    private InetAddress group;
    public MulticastSocket mcst;
    
    public MDBChannel(String IP, int PORT, int peerID) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        this.peerID = peerID;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
    }

    @Override
    public void run()
    {
        try
        {
            //Join Multicast Channel
            mcst.joinGroup(group);
            
            //The socket that sent the package should not receive it back
            //Atention: only wotks if the peers are in different machines!
            //mcst.setLoopbackMode(true);
            
            int chunkSize = 64000;
            byte[] buff = new byte[chunkSize + 1000];
            DatagramPacket dp = new DatagramPacket(buff, buff.length);
            
            while(true)
            {
                mcst.receive(dp);
                
                String message = new String(dp.getData(), 0, dp.getLength());
                
                //Split the message into its components Header and Body
                String[] msgComponents = message.split("\r\n\r\n");
                String msgHeader = msgComponents[0];
                String msgBody;
                
                if (msgComponents.length > 1 )
                {
                    msgBody = msgComponents[1];
                }
                else
                {
                    msgBody = "";
                }
                
                String[] header = msgHeader.split(" ");
                
                //alternative for the loopback mode when the peers are running on the same machine
                if((header[2]).equals(Integer.toString(peerID)))
                    return;
                
                System.out.println("Message received: \n"+message);
                
                
                switch(header[0])
                {
                    case "PUTCHUNK":
                        BackupHandler bh = new BackupHandler(
                                Integer.parseInt(header[2]), 
                                header[3], 
                                Integer.parseInt(header[4]),
                                msgBody.getBytes());
                        if(bh.store())
                        {
                            System.out.println("funciona!");
                        }
                        else
                        {
                            System.out.println("nao funciona!");
                        }
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MDBCHANNEL]: Error receiving package!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
