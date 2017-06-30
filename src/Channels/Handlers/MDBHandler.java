package Channels.Handlers;

import Handlers.BackupHandler;
import java.net.DatagramPacket;

public class MDBHandler extends Thread
{
    private final DatagramPacket packet;
    private final int peerID;
    
    public MDBHandler(DatagramPacket packet, int peerID)
    {
        this.packet = packet;
        this.peerID = peerID;
    }

    @Override
    public void run()
    {
        String message = new String(packet.getData(), 0 , packet.getLength());
        String[] msgComponents = message.split("\r\n\r\n");
        String header = msgComponents[0];
        String body = (msgComponents.length > 1)? msgComponents[1] : "";
        String[] headerComponents = header.split(" ");
        /*
        headerComponents:
        [0] - Operation
        [1] - Protocol Version
        [2] - Sender ID
        [3] - File ID
        [4] - Chunk Nº
        [5] - Replicatioon Degree
        [6] - "FROMRECLAIM" (ONLY EXISTS IF MESSAGE WAS ORIGINATED FROM A RECLAIM OPERATION)
        */
        
        if(peerID == Integer.parseInt(headerComponents[2]))
            return;
        
        if(headerComponents.length == 7)
            Peer.Peer.rd.add(headerComponents[3], Integer.parseInt(headerComponents[4]), Integer.parseInt(headerComponents[2]));
        
        System.out.println("[MDB HANDLER]: RECEIVED MESSAGE: "+header);
        
        Peer.Peer.rd.setDesiredReplicationDegree(headerComponents[3], Integer.parseInt(headerComponents[5]));
        BackupHandler handler = new BackupHandler(Float.parseFloat(headerComponents[1]),
                                                  peerID,
                                                  headerComponents[3],
                                                  Integer.parseInt(headerComponents[4]),
                                                  body.getBytes());
        handler.start();
    }
    
    
}
