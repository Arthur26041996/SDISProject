package Channels.Handlers;

import Handlers.DeleteHandler;
import Senders.MDBSender;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCHandler extends Thread
{
    private final DatagramPacket packet;
    private final int peerID;
    
    public MCHandler(DatagramPacket packet, int peerID)
    {
        this.packet = packet;
        this.peerID = peerID;
    }

    @Override
    public void run()
    {
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] header = message.split(" ");
        
        /*
        header:
        [0] - Message Type
        [1] - Protocol Version
        [2] - Sender ID
        [3] - File ID
        [4] - Chunk Nº (for Message Type 'STORED' & 'REMOVED')
        */
        
        if(peerID == Integer.parseInt(header[2]))
        {
            return;
        }
        
        System.out.println("[MC HANDLER]: RECEIVED MESSAGE: "+message.trim());
        
        switch(header[0])
        {
            case "STORED":
                Peer.Peer.rd.add(header[3], Integer.parseInt(header[4].trim()), Integer.parseInt(header[2]));
                break;
                
            case "DELETE":
                DeleteHandler dh = new DeleteHandler(peerID, header[3].trim());
                dh.start();
                break;
                
            case "REMOVED":
                Peer.Peer.rd.removePeer(header[3], Integer.parseInt(header[4]), Integer.parseInt(header[2]));
                
                File peer = new File("FileSystem//Peer_"+Peer.Peer.getPeerID());
                File[] files = peer.listFiles();
                
                for(File file : files)
                {
                    if(file.getName().equals(header[3]))
                    {
                        File[] chunks = file.listFiles();
                        for(File chunk : chunks)
                        {
                            if(chunk.getName().equals("Chunk_"+(header[4].trim())))
                            {
                                try
                                {
                                    Thread.sleep((new Random()).nextInt(401));
                                    if(Peer.Peer.rd.getReplicationDegree(header[3], Integer.parseInt(header[4].trim())) < Peer.Peer.rd.getDesiredReplicationDegree(header[3]))
                                    {
                                        message = "PUTCHUNK "
                                                  +Peer.Peer.getProtVersion()+" "
                                                  +Peer.Peer.getPeerID()+" "
                                                  +chunk.getName()+" "
                                                  +chunk.getName().split("_")[1].split("\\.")[0]
                                                  +Peer.Peer.rd.getDesiredReplicationDegree(file.getName())
                                                  +"\r\n\r\n";

                                        byte[] msg = message.getBytes();

                                        byte[] cnk = Files.readAllBytes(chunk.toPath());
                                        byte[] body = new byte[msg.length + cnk.length];
                                        System.arraycopy(msg, 0, body, 0, msg.length);
                                        System.arraycopy(cnk, 0, body, msg.length, cnk.length);

                                        DatagramPacket pkt = new DatagramPacket(body, body.length, InetAddress.getByName(Peer.Peer.getMdbIP()), Peer.Peer.getMdbPort());
                                        DatagramSocket socket = new DatagramSocket();
                                        socket.send(pkt);
                                    }              
                                    return;
                                }
                                catch (InterruptedException ex)
                                {
                                    System.out.println("[MC HANDLER]: ERROR IN METHOD \'Thread.sleep()\'");
                                    ex.printStackTrace();
                                    return;
                                } catch (IOException ex)
                                {
                                    Logger.getLogger(MCHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
                break;
                
                
                /*
                int sleepTime = (new Random()).nextInt(401);
        
                try
                {
                    Thread.sleep(sleepTime);
                    if(Peer.Peer.rd.getReplicationDegree(header[3], Integer.parseInt(header[4].trim())) < Peer.Peer.rd.getDesiredReplicationDegree(header[3]))
                    {
                        MDBSender sender = new MDBSender(Peer.Peer.getMdbIP(),
                                                         Peer.Peer.getMdbPort(),
                                                         Float.parseFloat(header[1]),
                                                         peerID,
                                                         header[3],
                                                         Peer.Peer.rd.getDesiredReplicationDegree(header[3]));
                        sender.start();
                    }
                } catch (InterruptedException ex)
                {
                    System.out.println("[MC HANDLER]: ERROR IN METHOD \'Thread.sleep()\'");
                    ex.printStackTrace();
                } 
                catch (UnknownHostException ex)
                {
                    System.out.println("[MC HANDLER]: UNKNOWN HOST");
                    ex.printStackTrace();
                } 
                catch (SocketException ex)
                {
                    System.out.println("[MC HANDLER]: FAILED ATTEMPTING TO OPEN SOCKET");
                    ex.printStackTrace();
                }
                */
                
            default:
                System.out.println("[MC HANDLER]: RECEIVED MESSAGE CONSTAINS AN INVALID OPERATION");
                break;
        }
    }
}
