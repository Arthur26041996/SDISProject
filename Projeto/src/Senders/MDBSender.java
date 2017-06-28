package Senders;

import Handlers.FileHandler;
import Objects.Chunk;
import Peer.RepDegree;
import Peer.Peer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class MDBSender extends Thread
{
    private final InetAddress address;
    private final int port;
    private final DatagramSocket socket;
    private DatagramPacket packet;
    private final String file;
    private final int repDegree;
    private FileHandler fh;
    private LinkedList<Chunk> chunks;
    private final float version;
    private final int peerID;
    
    public MDBSender(String ip, int port, float version, int peerID, String file, int repDegree) throws UnknownHostException, SocketException
    {
        address = InetAddress.getByName(ip);
        this.port = port;
        socket = new DatagramSocket();
        
        this.version = version;
        this.peerID = peerID;
        
        this.file = file;
        this.repDegree = repDegree;
    }

    @Override
    public void run()
    {
        fh = new FileHandler();
        chunks = fh.splitFile(file);
        int request = Peer.state.addRequest(file, chunks.getFirst().FileId, repDegree);
        Peer.mc.setNewRequest(request);
        
        byte[] header, body;
        
        System.out.println("[MDB SENDER]: DESIRED REPLICATION DEGREE: "+repDegree);
        System.out.println("[MDB SENDER]: CHUNK LIST SIZE = "+chunks.size());
        for(Chunk c : chunks)
        {
            System.out.println("[MDB SENDER]: SIZE OF CHUNK "+c.getChunkNo()+" = "+c.Chunk.length+" bytes");
        
            Peer.state.setChunk(request, c.ChunkNo, Peer.rd.getReplicationDegree(c.FileId, c.ChunkNo));
            
            String message = "PUTCHUNK " + 
                              version + " " +
                              peerID + " " + 
                              c.FileId + " " +
                              c.ChunkNo + " " +
                              repDegree +
                              "\r\n\r\n";
            header = message.getBytes();
            
            body = new byte[header.length + c.Chunk.length];
            System.arraycopy(header, 0, body, 0, header.length);
            System.arraycopy(c.Chunk, 0, body, header.length, c.Chunk.length);
            
            packet = new DatagramPacket(body, body.length, address, port);
            
            int sleepTime = 1000;
            int iteration = 0;
            
            System.out.println("[MDB SENDER]: SENDING CHUNK "+c.ChunkNo);
            while(Peer.rd.getReplicationDegree(c.FileId, c.ChunkNo) < repDegree && (iteration++ < 5))
            {
                try
                {   
                    System.out.println("[MDB SENDER]: ATTEMPT "+iteration);
                    socket.send(packet);
                    Thread.sleep(sleepTime);
                    sleepTime *= 2;
                }
                catch (IOException ex)
                {
                    System.out.println("[MDB SENDER]: FAILED ATTEMPTING TO SEND CHUNK "+c.ChunkNo);
                    System.out.println(ex.getMessage());
                }
                catch (InterruptedException ex)
                {
                    System.out.println("[MDB SENDER]: ERROR IN METHOD \'Thread.sleep(sleepTime);\'");
                    System.out.println(ex.getMessage());
                }
            }
                System.out.println("[MDB SENDER]: CHUNK "+c.ChunkNo+" - PERCEIVED REPLICATION DEGREE "+Peer.rd.getReplicationDegree(c.FileId, c.ChunkNo));
        }
        System.out.println("\n\n\n");
    }
}
