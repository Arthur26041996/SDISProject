package Channel;

import Peer.CustomFileSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
public class MCChannel implements Runnable
{
    private final String IP;
    public int PORT;
    public InetAddress group;
    public MulticastSocket mcst;
    private boolean chunkRec;

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
                        
                    case "GETCHUNK":
                        CustomFileSystem a = new CustomFileSystem();
                        try
                        {       
                                //Mira si existe, sino nullpointerexception
                                File file = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "\\FileSystem" + 
                                        "\\Peer_" + Peer.Peer.getPeerId() + "\\"+ 
                                        header[3].trim() + "\\Chunk_" + header[4].trim() + ".txt");
                                 
                                //Crear datagrama con el chunk
                                byte[] bufHeader;
                                byte[] bufMsg;
                                DatagramPacket pack;
                                
                                
                                String msgHeader = "CHUNK " + 
                                Peer.Peer.getProtVersion() + " " +
                                Peer.Peer.getPeerId() + " " + 
                                header[3].trim() + " " +
                                header[4].trim() + " " +
                                "\r\n\r\n" +
                                "\r\n\r\n";
                                bufHeader=msgHeader.getBytes();
                                
                                //Transforma el file en array de bits
                                FileInputStream fis = new FileInputStream(file);
                                byte chunk[] = new byte[(int)file.length()];
                                fis.read(chunk);
                                
                                bufMsg=new byte[bufHeader.length + chunk.length];
                                 
                                
                                //public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                                System.arraycopy(bufHeader, 0, bufMsg, 0, bufHeader.length);
                                System.arraycopy(chunk, 0, bufMsg, bufHeader.length, chunk.length);
                                
                                //datagrama preparado para enviar por mdr
                                pack = new DatagramPacket(bufMsg, bufMsg.length, Peer.Peer.mdr.group,Peer.Peer.mdr.PORT);
                                
                    try {
                        //Esperar entre 0 y 400
                        Thread.sleep(100);
//int sleep = (int) (Math.random() * 400);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MCChannel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                                
                                
                                Peer.Peer.mdr.mcst.send(pack);
                                 
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
