package Channels.Handlers;

import Handlers.DeleteHandler;
import Handlers.RestoreHandler;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCHandler extends Thread
{
    private final DatagramPacket packet;
    private final int peerID;
    private final int requestID;
    
    public MCHandler(DatagramPacket packet, int peerID, int requestID)
    {
        this.packet = packet;
        this.peerID = peerID;
        this.requestID = requestID;
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
        [4] - Chunk Nº (for Message Type 'STORED')
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
                Peer.Peer.state.setChunk(requestID, Integer.parseInt(header[4]), Peer.Peer.rd.getReplicationDegree(header[3], Integer.parseInt(header[4])));
                break;
                
            case "DELETE":
                DeleteHandler dh = new DeleteHandler(peerID, header[3].trim());
                dh.start();
                break;
                
            case "REMOVED":
                System.out.println("[MC HANDLER]: 'REMOVED' MESSAGE RECEIVED");
                break;
             case "GETCHUNK":
                RestoreHandler rh = new RestoreHandler(peerID, header[3].trim(),Integer.parseInt(header[4].trim()));
                rh.start();
                break;       
            default:
                System.out.println("[MC HANDLER]: RECEIVED MESSAGE CONSTAINS AN INVALID OPERATION");
                break;
        }
    }
}
