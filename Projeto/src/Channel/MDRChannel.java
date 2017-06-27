package Channel;

import Handlers.FileHandler;
import Peer.Chunk;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MDRChannel implements Runnable
{
    private String IP;
    public int PORT;
    public InetAddress group;
    public MulticastSocket mcst;

    public MDRChannel(String IP, int PORT) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
        mcst.setTimeToLive(1);
        mcst.setLoopbackMode(false);
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
            FileHandler fh  = new FileHandler();
            
            while(true)
            {
                mcst.receive(dp);
                
                String message = new String(dp.getData(), 0, dp.getLength());
                
                //Split the message into its components Header and Body.
                //What separates both of them are the <CRLF> characters.
                String[] msgComponents = message.split("\r\n\r\n");
                String msgHeader = msgComponents[0]; //SI ES NECESARIO, para el num de chunk y el file id
                
                
                //Create the chunk
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
                if((header[2]).equals(Integer.toString(Peer.Peer.getPeerId())))
                    return;
                
                System.out.println("------------------------------------------------------");
                System.out.println("Message received: \n"+msgHeader);
                System.out.println("------------------------------------------------------\n");
                
                switch(header[0])
                {
                    case "CHUNK":
                        byte [] chunk;
                        if(msgComponents.length>2){
                     chunk = msgComponents[2].getBytes();
                        }
                        else{chunk=("".getBytes());}
                       
                    Chunk novoChunk= new Chunk(header[3], Integer.parseInt(header[4]), chunk);
                    int msToSleep = 1000;
                    Thread.sleep(msToSleep);
                    System.out.println("vuelvo al peer");
                    Peer.Peer.chunkListRestore(novoChunk);
                    
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
