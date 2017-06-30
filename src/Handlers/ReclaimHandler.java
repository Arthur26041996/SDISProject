package Handlers;

import Senders.MCSender;
import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ReclaimHandler extends Thread
{
    private long reclaim;
    private long total;
    private long used;
    private long free;
    
    public ReclaimHandler(long reclaim)
    {
        this.reclaim = reclaim;
        this.total = Peer.Peer.state.getTotalMem();
        this.used = Peer.Peer.state.getUsedMem();
        this.free = Peer.Peer.state.getAvailMem();
    }

    @Override
    public void run()
    {
        if(reclaim > total)
        {
            System.out.println("[RECLAIM HANDLER]: VALUE TO RECLAIM IS HIGHER THAN TOTAL MEMORY");
        }
        else if(reclaim == 0)
        {
            //Do nothing
        }
        else if(reclaim <= free)
        {
            System.out.println("[RECLAIM HANDLER]: "+reclaim+" BYTES RECLAIMED");
            free -= reclaim;
            total -= reclaim;
            Peer.Peer.state.setAvailMem(free);
            Peer.Peer.state.setTotalMem(total);
        }
        else
        {
            System.out.println("[RECLAIM HANDLER]: RECLAIMING "+reclaim+" BYTES");
            reclaim -= free;
            total -= free;
            free = 0;
            
            File peer = new File("FileSystem//Peer_"+Peer.Peer.getPeerID());
            File[] files = peer.listFiles();
            File[] chunks;
            File chunk;
            long chunkSize;
            
            for (File file : files)
            {
                chunks = file.listFiles();
                for (File chunk1 : chunks)
                {
                    chunk = chunk1;
                    chunkSize = chunk.length();
                    chunk.delete();
                    Peer.Peer.state.removeChunkStored(file.getName(), Integer.parseInt(chunk.getName().split("_")[1].split("\\.")[0]), false);
                    try
                    {
                        MCSender sender = new MCSender(Peer.Peer.getMcIP(),
                                Peer.Peer.getMcPort(),
                                Peer.Peer.getProtVersion(),
                                Peer.Peer.getPeerID(),
                                "RECLAIM");
                        sender.setFile(file.getName());
                        sender.setChunkNo(Integer.parseInt(chunk.getName().split("_")[1].split("\\.")[0]));
                        sender.start();
                        used -= chunkSize;
                        if(chunkSize >= reclaim)
                        {
                            total -= reclaim;
                            free = total - used;
                            Peer.Peer.state.setTotalMem(total);
                            Peer.Peer.state.setUsedMem(used);
                            Peer.Peer.state.setAvailMem(free);
                            return;
                        }
                        else
                        {
                            total -= chunkSize;
                            reclaim -= chunkSize;
                        }
                    }catch (SocketException ex)
                    {
                        System.out.println("[RECLAIM HANDLER]: FAILED ATTEMPTING TO OPEN SOCKET");
                        ex.printStackTrace();
                    }catch (UnknownHostException ex)
                    {
                        System.out.println("[RECLAIM HANDLER]: UNKNOW HOST");
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
