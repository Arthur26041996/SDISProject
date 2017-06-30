package Senders;

import Handlers.FileHandler;
import Objects.Chunk;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

public class MCSender extends Thread
{
    private final InetAddress address;
    private final int port;
    private final DatagramSocket socket;
    private final float version;
    private final int peerID;
    private final FileHandler fh;
    private final String messageType;
    private String file;
    private int chunkNo;
    private DatagramPacket packet;
    
    
    public MCSender(String ip, int port, float version, int peerID, String messageType) throws SocketException, UnknownHostException
    {
        this.address = InetAddress.getByName(ip);
        this.port = port;
        socket = new DatagramSocket();
        this.version = version;
        this.peerID = peerID;
        this.fh = new FileHandler();
        this.messageType = messageType;
    }
    
    public void setFile(String file)
    {
        this.file = file;
    }
    
    public void setChunkNo(int chunkNo)
    {
        this.chunkNo = chunkNo;
    }

    @Override
    public void run()
    {
        String message;
        switch(messageType)
        {
            case "DELETE":
                File file = new File(this.file);
                if(!file.exists())
                {
                    System.out.println("[MC SENDER]: FILE DOES NOT EXIST");
                    return;
                }
                String fileID = fh.hash(file.getName() + file.lastModified());
                message = "DELETE "
                          +version+" "
                          +peerID+" "
                          +fileID
                          +"\r\n\r\n";
                try
                {
                    packet = new DatagramPacket(message.getBytes(),
                                                message.getBytes().length,
                                                address,
                                                port);
                    socket.send(packet);
                    Peer.Peer.rd.removeFile(fileID);
                }
                catch (IOException ex)
                {
                    System.out.println("[MC SENDER]: FAILED ATTEMPTING TO SEND PACKET");
                    ex.printStackTrace();
                }
                break;
                
            case "STORED":
                message = "STORED "
                          +version+" "
                          +peerID+" "
                          +this.file+" "
                          +chunkNo;
                
                try
                {
                    packet = new DatagramPacket(message.getBytes(),
                                                message.getBytes().length,
                                                address,
                                                port);
                    int sleepTime = (new Random()).nextInt(401);
                    Peer.Peer.rd.add(this.file, chunkNo, peerID);
                    Thread.sleep(sleepTime);
                    socket.send(packet);
                }
                catch (IOException ex)
                {
                    System.out.println("[MC SENDER]: FAILED ATTEMPTION TO SEND PACKET");
                    ex.printStackTrace();
                }
                catch (InterruptedException ex)
                {
                    System.out.println("[MC SENDER]: ERROR IN METHOD \'Thread.sleep()\'");
                    ex.printStackTrace();
                }
                catch (Exception ex)
                {
                    System.out.println("[MC SENDER]: "+ex.getMessage());
                    ex.printStackTrace();
                }
                break;
                
            case "RECLAIM":
                message = "REMOVED "
                          +version+" "
                          +peerID+" "
                          +this.file+" "
                          +chunkNo;
                
                try
                {
                    packet = new DatagramPacket(message.getBytes(),
                                                message.getBytes().length,
                                                address,
                                                port);
                    socket.send(packet);
                }
                catch(IOException ex)
                {
                    System.out.println("[MC SENDER]: FAILED ATTEMPTION TO SEND PACKET");
                    ex.printStackTrace();
                }
                catch(Exception ex)
                {
                    System.out.println("[MC SENDER]: "+ex.getMessage());
                    ex.printStackTrace();
                }
                break;
                
            case "RESTORE":
                LinkedList<Chunk> chunk = fh.splitFile(this.file);
                if(chunk.isEmpty()){
                    System.out.println("[PEER - restore] : File doesn' exits"); 
                    return;
                }
                System.out.println("chunk list size: "+chunk.size());
                int tamanio = chunk.size();
                String fileId=chunk.get(0).getFileId();
                for(int i = 0; i < chunk.size(); i++)
                {
                    message = "GETCHUNK " + 
                    version + " " +
                    this.peerID + " " + 
                    fileId + " " +
                    i+
                    "\r\n\r\n";
                    try
                        {
                            packet = new DatagramPacket(message.getBytes(),
                                                        message.getBytes().length,
                                                        address,
                                                        port);
                            socket.send(packet);
                        }
                        catch(IOException ex)
                        {
                            System.out.println("[MC SENDER]: FAILED ATTEMPTION TO SEND PACKET");
                            ex.printStackTrace();
                        }
                        catch(Exception ex)
                        {
                            System.out.println("[MC SENDER]: "+ex.getMessage());
                            ex.printStackTrace();
                        }
                }
                break;
                
            default:
                System.out.println("[MC SENDER]: INVALID MESSAGE TYPE");
                break;
        }
    }
}
