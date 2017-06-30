/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import Senders.MDRSender;

/**
 *
 * @author alba
 */
public class RestoreHandler extends Thread{
    private final String fileName;
    private final File file;
    private final int peerID;
    private final int numChunk;
    
    public RestoreHandler(int peerID, String fileID, int numChunk)
    {
        this.peerID = peerID;
        this.file = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "\\FileSystem" + 
                                        "\\Peer_" + peerID + "\\"+ 
                                        fileID + "\\Chunk_" + numChunk + ".txt");
        this.numChunk=numChunk;
        fileName=fileID;
    }
    
     @Override
    public void run()
    {
        try
        {
                               
        //Transforma el file en array de bits
        FileInputStream fis;
        fis = new FileInputStream(file);
        byte chunk[] = new byte[(int)file.length()];
        System.out.println("LONG DEL CHUNK" + chunk.length);
        fis.read(chunk);
        MDRSender sender = new MDRSender(Peer.Peer.getMdrIP(), Peer.Peer.getMdrPort(), Peer.Peer.getProtVersion(), 
                peerID, fileName,numChunk,chunk); 
        
//int sleep = (int) (Math.random() * 400);
        }
         catch (FileNotFoundException ex) {
                                    Logger.getLogger(RestoreHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                                    Logger.getLogger(RestoreHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
