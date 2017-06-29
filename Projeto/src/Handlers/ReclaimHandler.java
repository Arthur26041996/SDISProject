package Handlers;

import Senders.MCSender;
import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ReclaimHandler extends Thread
{
    private long memToReclaim;
    private File file;
    private File[] aux;
    private File[] files;

    public ReclaimHandler(long memToReclaim)
    {
        this.memToReclaim = memToReclaim;
        this.file = new File("FileSystem\\Peer_"+Peer.Peer.getPeerID());
        this.files = file.listFiles();
    }

    @Override
    public void run()
    {
        if(memToReclaim > Peer.Peer.state.getTotalMem())
        {
            System.out.println("[RECLAIM HANDLER]: VALUE TO RECLAIM IS HIGHER THAN TOTAL STORAGE SPACE RESERVED");
            System.out.println("[RECLAIM HANDLER]: COULD NOT RECLAIM STORAGE SPACE");
            return;
        }
        else if(memToReclaim <= Peer.Peer.state.getAvailMem())
        {
            Peer.Peer.state.setTotalMem(Peer.Peer.state.getTotalMem() - memToReclaim);
            Peer.Peer.state.setAvailMem(Peer.Peer.state.getAvailMem() - memToReclaim);
            return;
        }
        else if(memToReclaim < Peer.Peer.state.getTotalMem() && memToReclaim > Peer.Peer.state.getUsedMem())
        {
            memToReclaim -= Peer.Peer.state.getAvailMem();
            Peer.Peer.state.setAvailMem(0);
            Peer.Peer.state.setTotalMem(Peer.Peer.state.getTotalMem() - memToReclaim);
        }
        
        if(files != null)
        {
            memToReclaim -= Peer.Peer.state.getAvailMem();
            Peer.Peer.state.setTotalMem(Peer.Peer.state.getTotalMem() - Peer.Peer.state.getAvailMem());
            for(int i = 0; i < files.length && (Peer.Peer.state.getUsedMem() + memToReclaim) > Peer.Peer.state.getTotalMem(); i++)
            {
                file = files[i];
                aux = file.listFiles();
                for(int j = 0; j < aux.length && (Peer.Peer.state.getUsedMem() + memToReclaim) > Peer.Peer.state.getTotalMem(); j++)
                {
                    aux[j].delete();
                    Peer.Peer.state.removeChunkStored(file.getName(), j);
                    Peer.Peer.rd.removePeer(file.getName(), j, Peer.Peer.getPeerID());
                    
                    try
                    {
                        MCSender sender = new MCSender(Peer.Peer.getMcIP(),
                                Peer.Peer.getMcPort(),
                                Peer.Peer.getProtVersion(),
                                Peer.Peer.getPeerID(),
                                "RECLAIM");
                        sender.setChunkNo(j);
                        sender.setFile(file.getName());
                        sender.start();
                    }
                    catch (SocketException ex)
                    {
                        System.out.println("[RECLAIM HANDLER]: ERROR ATTEMPTING TO OPEN SOCKET");
                        ex.printStackTrace();
                    }
                    catch (UnknownHostException ex)
                    {
                        System.out.println("[RECLAIM HANDLER]: UNKNOWN HOST");
                        ex.printStackTrace();
                    }
                }
            }
            Peer.Peer.state.setAvailMem(0);
            Peer.Peer.state.setTotalMem(Peer.Peer.state.getTotalMem() - memToReclaim);
        }
        else
        {
            System.out.println("[RECLAIM HANDLER]: NO FILE TO DELETE");
        }
    }
}
