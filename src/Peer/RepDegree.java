package Peer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RepDegree
{
    private final Map<String, Map<Integer, LinkedList<Integer>>> chunk;
    
    public RepDegree()
    {
        chunk = new HashMap<>();
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
                temp.put(chunkNo, null);
                chunk.replace(fileID, temp);
            }
        }   
    }
    
    public void addNewPeer(String fileID, int chunkNo, int peerID)
    {
        if(chunk.containsKey(fileID) && chunk.get(fileID).containsKey(chunkNo))
        {
            if(chunk.get(fileID).get(chunkNo) == null)
            {
                LinkedList<Integer> peers = new LinkedList<>();
                peers.add(peerID);
                chunk.get(fileID).replace(chunkNo, peers);
            }
            else if(!chunk.get(fileID).get(chunkNo).contains(peerID))
            {
                chunk.get(fileID).get(chunkNo).add(peerID);
            }
        }
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
        return 0;
    }
}
