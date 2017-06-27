package Channel;

import Handlers.BackupHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;

public class MDBChannel implements Runnable
{
    private final String IP;
    private final int PORT;
    private final int peerID;
    private final InetAddress group;
    public MulticastSocket mcst;
    
    
    public MDBChannel(String IP, int PORT, int peerID) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        this.peerID = peerID;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
        mcst.setTimeToLive(1);
        mcst.setLoopbackMode(false); //Set this property to true if running the peers
                                     //in different machines.
    }

    @Override
    public void run()
    {
        try
        {
            //Join Multicast Channel
            mcst.joinGroup(group);
            
            int chunkSize = 64000;
            //As the package received contains the message Header plus its body,
            //which consists of the chunk with a (maximum) size of 64Kb, the buffer
            //will need more than 64Kb to store the whole message.
            byte[] buff = new byte[chunkSize + 1000];
            DatagramPacket dp = new DatagramPacket(buff, buff.length);
            
            while(true)
            {
                mcst.receive(dp);
                
                String message = new String(dp.getData(), 0, dp.getLength());
                
                //Split the message into its components Header and Body.
                //What separates both of them are the <CRLF> characters.
                String[] msgComponents = message.split("\r\n\r\n");
                String msgHeader = msgComponents[0];
                String msgBody;
                if (msgComponents.length > 1 )
                {
                    //In case of the file size don't be a multiple
                    //of 64Kb.
                    msgBody = msgComponents[1];
                }
                else
                {
                    //In case of the file size be an exact multiple of 64KB,
                    //the last chunk will have size 0. To prevent null pointer
                    //exception, a string is created with length 0.
                    msgBody = "";
                }
                
                //Get the info from the header separatedly
                //0 - <MESSAGE_TYPE>
                //1 - <VERSION>
                //2 - <SENDER_ID>
                //3 - <FILE_ID>
                //4 - <CHUNK_NO>
                //5 - <REP_DEGREE>
                String[] header = msgHeader.split(" ");
                
                //alternative for the loopback mode not working when peers
                //are running on the same machine. Compares the sender ID with
                //the receiver ID. If it's a match, ignore the package.
                if((header[2]).equals(Integer.toString(peerID)))
                    return;
                
                System.out.println("------------------------------------------------------");
                System.out.println("Message received: \n"+msgHeader);
                System.out.println("------------------------------------------------------\n");
                
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
                            //After successfully storing the file, the receiving peer
                            //should respond with a "STORED" message through the Control
                            //Channel.
                            //Message: "STORED <VERSION> <SENDER_ID> <FILE_ID> <CHUNK_NO>"
                            String response = "STORED " + 
                                            header[1] + " " +
                                            peerID + " " +
                                            header[3] + " " +
                                            header[4];
                            
                            DatagramPacket resp = new DatagramPacket(response.getBytes(),
                                                                     response.getBytes().length,
                                                                     Peer.Peer.mc.group,
                                                                     Peer.Peer.mc.PORT);
                            
                            int msToSleep = (new Random()).nextInt(401);
                            Thread.sleep(msToSleep);
                            Peer.Peer.mc.mcst.send(resp);
                            
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
        catch (InterruptedException ex)
        {
            System.out.println("[MDBCHANNEL]: Error in Thread!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
