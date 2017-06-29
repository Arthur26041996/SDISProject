package Peer;

import Objects.Request;
import Util.CustomFileSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class State
{
    private final CustomFileSystem cfs;
    
    private final int peerId;
    private long totalMem;
    private long usedMem;
    private long availMem;
    private final List<Request> requests;
    private final Map<String, Map<Integer, Long>> stored; // File ID -> Chunks -> Chunk Size (in bytes)
    
    public State(int peerId, long totalMem)
    {
        this.peerId = peerId;
        
        this.totalMem = totalMem;
        usedMem = 0;
        availMem = totalMem - usedMem;
        
        requests = new LinkedList<>();
        stored = loadStoredChunks();
        
        cfs = new CustomFileSystem();
        cfs.createControlDir(peerId);
    }
    
    public int getPeerId()
    {
        return peerId;
    }

    public void setTotalMem(long value)
    {
        this.totalMem = value;
    }
    
    public long getTotalMem()
    {
        return totalMem;
    }

    public void setUsedMem(long value)
    {
        this.usedMem = value;
    }
    
    public long getUsedMem()
    {
        return usedMem;
    }

    public void setAvailMem(long value)
    {
        this.availMem = value;
    }
    
    public long getAvailMem()
    {
        return availMem;
    }

    /*--------------------------------------------------------------------------------------------------------------------*/
    /*Methods that handle with the backup requests and its chunks*/
    
    public List<Request> getRequests()
    {
        return requests;
    }
    
    public int addRequest(String filePath, String fileID, int repDegree)
    {
        int id = 100000 + (new Random()).nextInt(900000);
        Request rh = new Request(id);
        
        while(requests.contains(rh))
        {
            id = 100000 + (new Random()).nextInt(900000);
            rh.setId(id);
        }
        
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        
        rh.setDate(df.format(new Date()));
        rh.setPathName(filePath);
        rh.setFile(fileID);
        Peer.rd.setDesiredReplicationDegree(fileID, repDegree);
        requests.add(rh);
        
        saveRequests();
        
        return id;
    }
    
    public boolean addChunkToRequest(int requestID, int chunkNo)
    {
        for(Request r : requests)
        {
            if(r.getId() == requestID)
            {
                r.setChunks(chunkNo);
                return true;
            }
        }
        return false;
    }
    
    private void saveRequests()
    {
        String path = cfs.newSubDir("Peer_"+peerId, "Control");
        
        File file = new File(path+"//Requests.txt");
        StringBuilder sb = new StringBuilder();
        
        for(Request rh : requests)
        {
            sb.append("ID ").append(rh.getId()).append("\r\n");
            sb.append("DATE ").append(rh.getDate()).append("\r\n");
            sb.append("FILE_PATH ").append(rh.getPathName()).append("\r\n");
            sb.append("FILE_ID ").append(rh.getFile()).append("\r\n");
            sb.append("DES_REP_DEG ").append(Peer.rd.getDesiredReplicationDegree(rh.getFile())).append("\r\n");
            for(int chunk : rh.getChunks())
            {
                sb.append("CHUNK ").append(chunk).append("\r\n");
                sb.append("REP_DEG ").append(Peer.rd.getReplicationDegree(rh.getFile(), chunk)).append("\r\n");
            }
            sb.append("\r\n\r\n");
        }
        try
        {
            Files.write(file.toPath(), String.valueOf(sb).getBytes());
        }
        catch (IOException ex)
        {
            System.out.println("[STATE]: FAILED ATTEMPTING TO SAVE REQUESTS");
            ex.printStackTrace();
        }
    }
    
    /*--------------------------------------------------------------------------------------------------------------------*/
    /*Methods that handle the stored chunks*/
    
    public Map<String, Map<Integer, Long>> getChunksStored()
    {
        return stored;
    }
    
    public long getChunksSize(String fileId, int chunkNo)
    {
        return (stored.get(fileId)).get(chunkNo);
    }
    
    public int setChunksStored(String fileId, int chunkNo, long chunkSize)
    {
        if(stored.containsKey(fileId))
        {
            if(!stored.get(fileId).containsKey(chunkNo))
            {
                if(chunkSize < availMem)
                {
                    (stored.get(fileId)).put(chunkNo, chunkSize);
                    usedMem += chunkSize;
                    availMem -= chunkSize;
                }
                else
                    return -2;
            }
            else
                return -1;
        }
        else
        {
            if(chunkSize < availMem)
            {
                Map<Integer, Long> temp = new HashMap<>();
                temp.put(chunkNo, chunkSize);
                stored.put(fileId, temp);
                usedMem += chunkSize;
                availMem -= chunkSize;
            }
            else
                return -2;
        }
        return 1;
    }
    
    public boolean removeFileChunksStored(String fileId)
    {
        if(!stored.containsKey(fileId))
            return false;
        
        long totSize = 0;
        for(long i : (stored.get(fileId)).values())
        {
            totSize += i;
        }
        availMem += totSize;
        usedMem -= totSize;
        stored.remove(fileId);
        return true;
    }
    
    public boolean removeChunkStored(String fileId, int chunkNo)
    {
        if(!stored.containsKey(fileId))
            return false;
        
        long size = stored.get(fileId).get(chunkNo);
        availMem += size;
        usedMem -= size;
        stored.get(fileId).remove(chunkNo);
        return true;
    }
    
    private Map<String, Map<Integer, Long>> loadStoredChunks()
    {
        File fl = new File("FileSystem//Peer_"+peerId);
        Map<Integer, Long> chunk;
        Map<String, Map<Integer, Long>> file = new HashMap<>(); 
        long size = 0;
        
        if(fl.listFiles() == null)
            return new HashMap<>();
        
        for(File f : fl.listFiles())
        {   
            if(f.getName().equals("Control")) continue;
            
            chunk = new HashMap<>();
            
            for(File g : f.listFiles())
            {
                chunk.put(Integer.parseInt(g.getName().split("_")[1].split("//.")[0]), g.length());
                size += g.length();
            }
            file.put(f.getName(), chunk);
        }
        availMem -= size;
        usedMem += size;
        return file;
    }
    
    /*--------------------------------------------------------------------------------------------------------------------*/
    
    public StringBuilder print()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("------------------------------------------------\n");
        sb.append("\t\tBACKUP REQUESTS\n");
        sb.append("------------------------------------------------\n");
        for(Request rh : requests)
        {
            sb.append("Request ID: ").append(rh.getId());
            sb.append("\nRequest Date: ").append(rh.getDate());
            sb.append("\nFile pathname: ").append(rh.getPathName());
            sb.append("\nFile ID: ").append(rh.getFile());
            sb.append("\nDesired Replication Degree: ").append(Peer.rd.getDesiredReplicationDegree(rh.getFile()));
            for(int chunk : rh.getChunks())//Peer.rd.getChunks(rh.getFile()))
            {
                sb.append("\nChunk No ").append(chunk);
                sb.append(" - Replication Degree: ").append(Peer.rd.getReplicationDegree(rh.getFile(), chunk));
            }
            sb.append("\n\n");
        }
        sb.append("------------------------------------------------\n");
        sb.append("\t\tSTORED CHUNKS\n");
        sb.append("------------------------------------------------\n");
        for(String i : stored.keySet())
        {
            sb.append("File ID: ").append(i);
            for(int j : stored.get(i).keySet())
            {
                sb.append("\nChunkNo ").append(j).append(" - ");
                sb.append("Size: ").append((stored.get(i)).get(j)).append(" bytes").append(" - ");
                sb.append("Perceived Replication Degree: ").append(Peer.rd.getReplicationDegree(i, j));
            }
            sb.append("\n\n");
        }
        sb.append("------------------------------------------------\n");
        sb.append("\t\tSTORAGE INFO\n");
        sb.append("------------------------------------------------\n");
        sb.append("Reserved: ").append(totalMem/1000).append(" Kb");
        sb.append("\nUsed: ").append(usedMem/1000).append(" Kb");
        sb.append("\nFree: ").append(availMem/1000).append(" Kb");
        sb.append("\n\n");
        
        return sb;
    }
}
