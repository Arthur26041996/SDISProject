package Handlers;

import Util.CustomFileSystem;
import java.io.File;

public class DeleteHandler extends Thread
{
    private final File file;
    private final int peerID;
    private final CustomFileSystem cfs;
    
    public DeleteHandler(int peerID, String fileID)
    {
        this.peerID = peerID;
        this.file = new File("FileSystem//Peer_"+peerID+"//"+fileID);
        this.cfs = new CustomFileSystem();
    }

    @Override
    public void run()
    {
        cfs.removeDir(file);
        if(!Peer.Peer.state.removeFileChunksStored(file.getName()))
        {
            System.out.println("[DELETE HANDLER - STATE]: ERROR WHEN ATTEMPTING TO DELETE FILE - FILE NOT FOUND");
        }
    }
}
