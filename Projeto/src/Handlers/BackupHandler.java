package Handlers;

import Peer.CustomFileSystem;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class BackupHandler
{
    private int senderID;
    private String fileID;
    private int chunkNo;
    private byte[] chunk;
    private CustomFileSystem cfs;
    
    public BackupHandler(int senderID, String fileID, int chunkNo, byte[] chunk)
    {
        this.senderID = senderID;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.chunk = chunk;
    }
    
    public boolean store()
    {
        cfs = new CustomFileSystem();
        String path = cfs.newDir(fileID);
        
        try
        {
            Files.write(new File(path + "\\Chunk_" + chunkNo + ".txt").toPath(), chunk);
            return true;
        }
        catch(Exception e)
        {
            System.out.println("[BACKUP HANDLER]: could not write file!\n");
            e.printStackTrace();
            return false;
        }
    }
}
