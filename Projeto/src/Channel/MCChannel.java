package Channel;

import Peer.CustomFileSystem;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MCChannel implements Runnable
{
    private String IP;
    public int PORT;
    public InetAddress group;
    public MulticastSocket mcst;

    public MCChannel(String IP, int PORT) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
        mcst.setTimeToLive(1);
        mcst.setLoopbackMode(false);//Set this property to true if running the peers
                                     //in different machines.
    }
    
    @Override
    public void run()
    {
        try
        {
            //Join Multicast Channel
            mcst.joinGroup(group);
            
            int messageSize = 64000;
            byte[] buff = new byte[messageSize];
            DatagramPacket dp = new DatagramPacket(buff, buff.length);
            
            while(true)
            {
                mcst.receive(dp);
                
                String message = new String(dp.getData(), 0, dp.getLength());
                String[] header = message.split(" ");
                
                //alternative for the loopback mode not working when peers
                //are running on the same machine. Compares the sender ID with
                //the receiver ID. If it's a match, ignore the package.
                if(Integer.parseInt(header[2]) == Peer.Peer.getPeerId())
                   return;
                
                System.out.println("------------------------------------------------------");
                System.out.println("Received Message: "+message);
                System.out.println("------------------------------------------------------\n");
                               
                switch(header[0])
                {
                    case "STORED":
                        try
                        {
                            Peer.Peer.rdr.increaseActualRepDegree(header[3]);
                        }
                        catch(Exception ex)
                        {
                            return;
                        }
                        break;
                        
                    case "DELETE":
                        CustomFileSystem cfs = new CustomFileSystem();
                        try
                        {
                            File file = new File("FileSystem\\Peer_"+Peer.Peer.getPeerId()+"\\"+header[3].trim());
                            cfs.removeDir(file);
                        }
                        catch(NullPointerException ex)
                        {
                            ex.printStackTrace();
                        }
                        break;
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("[MCCHANNEL]: Error receiving package!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
