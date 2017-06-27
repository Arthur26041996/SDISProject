package Handlers;

import Peer.Chunk;
import Peer.Peer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

public class FileHandler
{
    private LinkedList<Chunk> Cnk = new LinkedList<Chunk>();
    
    public byte[] getChunk(String fileId, int chunkNo)
    {
        for(Chunk c : Cnk)
        {
            if(c.FileId.equals(fileId) && c.ChunkNo == chunkNo)
                return c.getChunk();
        }
        return null;
    }
    
    public LinkedList<Chunk> getAllChunks(String fileId)
    {
        LinkedList<Chunk> chunk = new LinkedList<Chunk>();
        for(Chunk c : Cnk)
        {
            if(c.FileId.equals(fileId))
                chunk.add(c);
        }
        return chunk;
    }
    
    public void addChunk(String fileId, int chunkNo, byte[] chunk)
    {
        System.out.println("ANIADO EL CHUNK");
        Chunk newChunk = new Chunk(fileId, chunkNo, chunk);
        Cnk.add(newChunk);
    }
    
    public LinkedList<Chunk> splitFile(String filePath)
    {
        File file = new File(filePath);
        long fileSize = file.length();
        int chunkQtd = (int)(fileSize/64000 + 1);
        
        //Maximum size of each chunk is 64Kb where K stands for 1000
        byte[] buffer;
        if(fileSize > 64000)
        {
            buffer = new byte[64000];
        }
        else
            buffer = new byte[(int)fileSize];
        
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)))
        {
            String encoded = encoding(file.getName());
            int tmp = 0;
            int count = 0;
            
            while((tmp = bis.read(buffer, 0, buffer.length)) > 0)
            {
                Chunk newChunk = new Chunk(encoded, count++, buffer);
                Cnk.add(newChunk);
                
                fileSize -= tmp;
                if(fileSize > 64000)
                    buffer = new byte[64000];
                else
                    buffer = new byte[(int)fileSize];
            }
            
            if(file.length() % 64000 == 0)
                Cnk.add(new Chunk(encoded, count++, new byte[0]));                
        }
        catch (IOException ex)
        {
            System.out.println("[FILEHANDLER - SPLITFILE]: Error spliting file!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
        return Cnk;
    }
    
    public String encoding(String fileName)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(fileName.getBytes("UTF-8"));
            String[] ascii = new String[bytes.length];
            StringBuilder builder = new StringBuilder();
            
            for(int i = 0, j = 0; i < bytes.length; i+=2, j++)
            {
                Character hex1 = Character.forDigit((bytes[i] >> 4) & 0xF, 16);
                hex1 = Character.forDigit((bytes[i] & 0xF), 16);
                Character hex2 = Character.forDigit((bytes[i+1] >> 4) & 0xF, 16);
                hex2 = Character.forDigit((bytes[i+1] & 0xF), 16);
                ascii[j] = Character.toString(hex1) + Character.toString(hex2);
                builder.append(ascii[j]);
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    
    //Pre: linked list of chunks is ordered by increasing chunkNo
  public File makeFile(){
        System.out.println("VOY A HACER EL FICHEROOOOO");
        File restored= new File(Paths.get(".").toAbsolutePath().normalize().toString() + "\\FileSystem" + 
                                        "\\Peer_" + Peer.getPeerId() + "\\recoveries\\"+Cnk.get(0).getFileId() + ".txt");
        FileOutputStream fileOuputStream;
        try {
        fileOuputStream = new FileOutputStream(restored);
        while(!(Cnk.isEmpty())){
            Chunk aux= Cnk.removeFirst();
            
		
                fileOuputStream.write(aux.getChunk());
                

		
        }
        fileOuputStream.close();
        } catch (Exception e) {
			//Manejar Error
                }         
        return restored;
    }
}
