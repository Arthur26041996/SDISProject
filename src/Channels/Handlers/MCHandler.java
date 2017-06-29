package Channels.Handlers;

import Handlers.DeleteHandler;
import Senders.MDBSender;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

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
                Peer.Peer.rd.addNewFile(header[3]);
                Peer.Peer.rd.addNewChunk(header[3], Integer.parseInt(header[4]));
                Peer.Peer.rd.addNewPeer(header[3], Integer.parseInt(header[4]), Integer.parseInt(header[2]));
                break;
                
            case "DELETE":
                DeleteHandler dh = new DeleteHandler(peerID, header[3].trim());
                dh.start();
                break;
                
            case "REMOVED":
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
                break;
                
            default:
                System.out.println("[MC HANDLER]: RECEIVED MESSAGE CONSTAINS AN INVALID OPERATION");
                break;
        }
    }
}
