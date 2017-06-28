package Handlers;

import Senders.MCSender;
import Util.CustomFileSystem;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class BackupHandler extends Thread
{
    private final float version;
    private final int peerID;
    private final String fileID;
    private final int chunkNo;
    private final byte[] chunk;
    private CustomFileSystem cfs;
    
    public BackupHandler(float version, int peerID, String fileID, int chunkNo, byte[] chunk)
    {
        this.version = version;
        this.peerID = peerID;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.chunk = chunk;
        this.cfs = new CustomFileSystem();
    }

    @Override
    public void run()
    {
        String path = cfs.newDir("Peer_" + peerID);
        String subDir = cfs.newSubDir(path, fileID);
        
        try
        {
            int answer = Peer.Peer.state.setChunksStored(fileID, chunkNo, chunk.length);
            if(answer == -2)
            {
                System.out.println("[BACKUP HANDLER]: AVAILABLE MEMORY IS NOT ENOUGH TO STORE THE FILE");
                return;
            }
            else if(answer == -1)
            {
                System.out.println("[BACKUP HANDLER]: FILE ALREADY EXIST");
            }
            else
            {
                Files.write(new File(subDir + "\\Chunk_" + chunkNo + ".txt").toPath(), chunk);
            }
            
            MCSender sender = new MCSender(Peer.Peer.getMcIP(),
                                       Peer.Peer.getMcPort(),
                                       version,
                                       peerID,
                                       "STORED");
            sender.setFile(fileID);
            sender.setChunkNo(chunkNo);
            sender.start();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("[BACKUP HANDLER]: UNKNOWN HOST");
            ex.printStackTrace();
        }
        catch (SocketException ex)
        {
            System.out.println("[BACKUP HANDLER]: FAILED ATTEMPTING TO OPEN SOCKET ");
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            Peer.Peer.state.removeChunkStored(fileID, chunkNo);
            System.out.println("[BACKUP HANDLER]: FAILED TO WRITE FILE");
            ex.printStackTrace();
        }
    }
}
