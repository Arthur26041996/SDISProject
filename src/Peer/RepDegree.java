package Peer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RepDegree
{
    private final Map<String, Map<Integer, LinkedList<Integer>>> chunk;
    private final Map<String, Integer> desiredReplicationDegree;
    
    public RepDegree()
    {
        chunk = new HashMap<>();
        desiredReplicationDegree = new HashMap<>();
    }
    
    public void add(String fileID, int chunkNo, int peerID)
    {
        
    }
    
    public void addNewFile(String fileID)
    {
        if(!chunk.containsKey(fileID))
        {
            Map<Integer, LinkedList<Integer>> map = new HashMap<>();
            chunk.put(fileID, map);
        }
    }
    
    public void addNewChunk(String fileID, int chunkNo)
    {
        if(chunk.containsKey(fileID))
        {
            if(!chunk.get(fileID).containsKey(chunkNo))
            {
                Map<Integer, LinkedList<Integer>> temp = new HashMap<>();
                LinkedList<Integer> peers = new LinkedList<>();
                temp.put(chunkNo, peers);
                chunk.replace(fileID, temp);
            }
        }   
    }
    
    public void addNewPeer(String fileID, int chunkNo, int peerID)
    {
        if(chunk.containsKey(fileID) && chunk.get(fileID).containsKey(chunkNo))
        {
            if(!chunk.get(fileID).get(chunkNo).contains(peerID))
            {
                chunk.get(fileID).get(chunkNo).add(peerID);
            }
        }
    }
    
    public List<String> getFiles()
    {
        List<String> files = new LinkedList<>();
        
        for(String file : chunk.keySet())
            files.add(file);
        
        return files;
    }
    
    public Map<Integer, LinkedList<Integer>> getChunks(String fileID)
    {
        if(chunk.containsKey(fileID))
            return chunk.get(fileID);
        else
            return null;
    }
    
    public List<Integer> getPeers(String fileID, int chunkNo)
    {
        return chunk.get(fileID).get(chunkNo);
    }
    
    public void removePeer(String fileID, int chunkNo, int peerID)
    {
        if(chunk.containsKey(fileID) && chunk.get(fileID).containsKey(chunkNo) && chunk.get(fileID).get(chunkNo).contains(peerID))
            chunk.get(fileID).get(chunkNo).remove(peerID);
    }
    
    public void removeChunk(String fileID, int chunkNo)
    {
        if(chunk.containsKey(fileID) && chunk.get(fileID).containsKey(chunkNo))
            chunk.get(fileID).remove(chunkNo);
    }
    
    public void removeFile(String fileID)
    {
        if(chunk.containsKey(fileID))
            chunk.remove(fileID);
    }
    
    public int getReplicationDegree(String fileID, int chunkNo)
    {
        if(chunk.containsKey(fileID) && chunk.get(fileID).containsKey(chunkNo))
            return chunk.get(fileID).get(chunkNo).size();
        return -1;
    }
    
    public void setDesiredReplicationDegree(String fileID, int replicationDegree)
    {
        desiredReplicationDegree.put(fileID, replicationDegree);
    }
    
    public int getDesiredReplicationDegree(String fileID)
    {
        if(desiredReplicationDegree.containsKey(fileID))
            return desiredReplicationDegree.get(fileID);
        return 0;
    }
}
