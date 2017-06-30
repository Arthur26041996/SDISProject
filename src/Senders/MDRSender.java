/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Senders;

import Handlers.FileHandler;
import Objects.Chunk;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alba
 */
public class MDRSender extends Thread {
    private final InetAddress address;
    private final int port;
    public final DatagramSocket socket;
    private DatagramPacket packet;
    private final float version;
    private final int peerID;
    private final int numChunk;
    private final String file;
    private final byte[] chunk;
    
    public MDRSender(String ip, int port, float version, int peerID, String file, int numChunk, byte[] chunk) throws UnknownHostException, SocketException
    {
        address = InetAddress.getByName(ip);
        this.port = port;
        socket = new DatagramSocket();
        this.version = version;
        this.peerID = peerID;
        this.file=file;
        this.numChunk=numChunk;
        this.chunk=chunk;
     }

    @Override
    public void run()
    {
        byte[] bufHeader;
        byte[] bufMsg;
        String msgHeader = "CHUNK " + 
        version + " " +
        peerID + " " +
        file + " " +
        numChunk + " " +
        "\r\n\r\n" +
        "\r\n\r\n";
        bufHeader=msgHeader.getBytes();
        bufMsg=new byte[bufHeader.length + chunk.length];
                                 
        System.out.println("bytes a enviar" + bufMsg.length);
        //public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(bufHeader, 0, bufMsg, 0, bufHeader.length);
        System.arraycopy(chunk, 0, bufMsg, bufHeader.length, chunk.length);
        packet = new DatagramPacket(bufMsg, bufMsg.length, address, port);
                                
        try {
            //datagrama preparado para enviar por mdr
            // pack = new DatagramPacket(bufMsg, bufMsg.length, Peer.Peer.mdr.group,Peer.Peer.mdr.PORT);
            //Esperar entre 0 y 400
            Thread.sleep(1000);
            socket.send(packet);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(MDRSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
