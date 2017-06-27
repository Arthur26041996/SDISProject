package Peer;

import Handlers.FileHandler;
import Channel.MCChannel;
import Channel.MDBChannel;
import Channel.MDRChannel;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer extends UnicastRemoteObject implements RemoteInterface
{
    private static String name;
    private static int peerId;
    private static float protVersion;
    private static MDBChannel mdb;
    public static MDRChannel mdr;
    public static MCChannel mc;
    private static String mdbIP;
    private static String mdrIP;
    private static String mcIP;
    private static int mdbPort;
    private static int mdrPort;
    private static int mcPort;
    private static Registry rg;
    private static FileHandler fh;
    public static RepDegRecord rdr;
    private boolean espera=false;
    private static LinkedList<Chunk> chunkList = new LinkedList<Chunk>();
    public static int tamanio =0;
    
    public Peer(String ap, int id, float version) throws RemoteException
    {
        super();
        name = ap;
        peerId = id;
        protVersion = version;  
    }
    
    public static void main(String args[])
    {   
        //Save Address and IP of the channels for further use
        mdbIP = args[0];
        mdbPort = Integer.parseInt(args[1]);
        mdrIP = args[2];
        mdrPort = Integer.parseInt(args[3]);
        mcIP = args[4];
        mcPort = Integer.parseInt(args[5]);
        
        try
        {
            Peer peer = new Peer(args[8], Integer.parseInt(args[7]), Float.parseFloat(args[6]));
           
            try
            {
                rg = LocateRegistry.createRegistry(1099);
            }
            catch(ExportException ex)
            {
                rg = LocateRegistry.getRegistry(1099);
            }
            rg.rebind(name, peer);
            
            //Initialize MDB, MDR & MC Channels
            startChannels(mdbIP, mdrIP, mcIP, mdbPort, mdrPort, mcPort);
        }
        catch(NumberFormatException | RemoteException ex)
        {
            System.out.println("[PEER - MAIN]: Error in remoting or starting channels!\nMessage: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static int getPeerId()
    {
        return peerId;
    }
    
    public static float getProtVersion(){
        return protVersion;
    }
    
    public static void startChannels(String mdbIP, String mdrIP, String mcIP, int mdbPort, int mdrPort, int mcPort)
    {
        try
        {
            mdb = new MDBChannel(mdbIP, mdbPort, peerId);
            mdr = new MDRChannel(mdrIP, mdrPort);
            mc = new MCChannel(mcIP, mcPort);
            //cT = System.currentTimeMillis();
            (new Thread(mdb)).start();
            (new Thread(mdr)).start();
            (new Thread(mc)).start();
        }
        catch (IOException ex)
        {
            System.out.println("[PEER]: Error initializing channels\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    @Override
    public void backUp(String path, int repDegree)
    {
        try
        {
            fh  = new FileHandler();
            LinkedList<Chunk> chunk = fh.splitFile(path);
            
            
            System.out.println("chunk list size: "+chunk.size());
            for(int i = 0; i < chunk.size(); i++)
            {
                System.out.println("size of chunk No"+chunk.get(i).ChunkNo+": "+chunk.get(i).Chunk.length);
            }
            
            //Send each chunk individually
            byte[] bufHeader;
            byte[] bufMsg;
            DatagramPacket pack;
            
            for(Chunk i : chunk)
            {
                //Message Header:
                //PUTCHUNK <VERSION> <SENDER_ID> <FILE_ID> <CHUNK_NO> <REP_DEGREE> <CRLF><CRLF>
                String msgHeader = "PUTCHUNK " + 
                                protVersion + " " +
                                peerId + " " + 
                                i.FileId + " " +
                                i.ChunkNo + " " +
                                repDegree +
                                "\r\n\r\n";
                bufHeader = msgHeader.getBytes();
                
                rdr = new RepDegRecord();
                rdr.addRecord(i.FileId, repDegree, 0);
                
                //Concatenates the bytes from the message Header with the bytes from the Chunk
                //in a new byte array.
                bufMsg = new byte[bufHeader.length + i.Chunk.length];
                System.arraycopy(bufHeader, 0, bufMsg, 0, bufHeader.length);
                System.arraycopy(i.Chunk, 0, bufMsg, bufHeader.length, i.Chunk.length);
                
                pack = new DatagramPacket(bufMsg, bufMsg.length, InetAddress.getByName(mdbIP), mdbPort);
                
                int msToSleep = 1000; //Set the time this thread should wait for responses
                                      //before sending the chunk again.
                int count = 0;
                
                System.out.println("Sending chunk No"+i.ChunkNo);
                while((rdr.getActualRepDegree(i.FileId) < repDegree) && (count < 5))
                {
                    mdb.mcst.send(pack);
                    System.out.println("Actual replication Degree: "+rdr.getActualRepDegree(i.FileId));
                    Thread.sleep(msToSleep);
                    msToSleep *= 2;
                    count++;
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("[PEER - BACKUP]: Error sending packet!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
        catch (InterruptedException ex)
        {
            System.out.println("[PEER - BACKUP]: Error in Thread.sleep!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void restore(String fileName)
    {
        System.out.println("RESTORE method called");
        fh  = new FileHandler();
            LinkedList<Chunk> chunk = fh.splitFile(fileName);
            if(chunk.isEmpty()){
                System.out.println("vacioooo"); 
                return;
            }
            
            System.out.println("chunk list size: "+chunk.size());
            tamanio = chunk.size();
            byte[] bufHeader;
            DatagramPacket pack;
            String fileId=chunk.get(0).getFileId();
            for(int i = 0; i < chunk.size(); i++)
            {
                String msgHeader = "GETCHUNK " + 
                getProtVersion() + " " +
                getPeerId() + " " + 
                fileId + " " +
                i + " " +
                "\r\n\r\n" +
                "\r\n\r\n";
                bufHeader=msgHeader.getBytes();
                System.out.println("NUM DE BYTES: " + bufHeader.length);              
                pack = new DatagramPacket(bufHeader, bufHeader.length,mc.group,
                                                                     mc.PORT);
                System.out.println("direccion mc " + mc.mcst);
                System.out.println("Asking for chunk " + i + " of the file " + fileName + " with id " + fileId);
               
            try {
                this.mc.mcst.send(pack);
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
                
            }
        
    }
    
    @Override
    public void delete(String fileName)
    {
        fh = new FileHandler();
        String fileID = fh.encoding(fileName);
        String message = "DELETE "+
                        protVersion + " " +
                        peerId + " " +
                        fileID +
                        "\r\n\r\n";
        
        try
        {
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(mcIP), mcPort);
            mc.mcst.send(dp);
        }
        catch (IOException ex)
        {
            System.out.println("[PEER - DELETE]: Error sending packet!\nMESSAGE: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void reclaim(int memoQuantity)
    {
        System.out.println("RECLAIM method called");
    }

    @Override
    public String state(int peerId)
    {
        System.out.println("STATED method called");
        return "";
    }
    
    public static void chunkListRestore(Chunk novoChunk){
        System.out.println("ENTROOOOO");
        System.out.println(chunkList.size());
        int chunkIndex=novoChunk.getChunkNo();
        System.out.println("Chunk num " + novoChunk.getChunkNo() + " INdex " + chunkIndex);
        if(chunkList.isEmpty()){
            chunkList.add(novoChunk);
            System.out.println("Almaceno chunk num " + novoChunk.getChunkNo() + " en posicion " + chunkIndex);
        }
        else{
            //Aniade (o no, si ya estaba) el chunk a la lista
            if((chunkList.size()-1)<chunkIndex){
                System.out.println("ENTRO EN PRIMER IF ");
                chunkIndex=0;
            }
            
                System.out.println("ENTRO EN SEGUNDO IF ");
                if(chunkList.get(chunkIndex).getChunkNo()== novoChunk.getChunkNo()){
                //no hacer nada, ese chunk ya esta en la list
                }
                else if(chunkList.get(chunkIndex).getChunkNo()> novoChunk.getChunkNo()){
                     System.out.println("POSICION MENOS ");
                    //Retroceder hasta encontrar uno menor o el mismo
                    chunkIndex--;
                    while(chunkList.get(chunkIndex).getChunkNo()>novoChunk.getChunkNo()){
                        chunkIndex--;
                    }
                    if(chunkList.get(chunkIndex).getChunkNo()<novoChunk.getChunkNo()){
                        System.out.println("Almaceno chunk num " + chunkList.get(chunkIndex).getChunkNo() 
                                + " en posicion " + chunkIndex);
                        chunkList.add((chunkIndex+1),novoChunk);
                    }
                    else{}//ES el mismo chunk, no hay que añadirlo
                }
                else { //chunkList.get(chunkIndex).getChunkNo()<novoChunk.getChunkNo()
                    //Retroceder hasta encontrar uno menor o el mismo
                    System.out.println("POSICION MAS ");
                    chunkIndex++;
                    if(chunkIndex>chunkList.size()-1){
                        System.out.println("Almaceno chunk num " + novoChunk.getChunkNo() 
                                + " en posicion " + chunkIndex);
                        chunkList.add(novoChunk);
                    }
                    while(chunkList.get(chunkIndex).getChunkNo()<novoChunk.getChunkNo()){
                        chunkIndex++;
                    }
                    if(chunkList.get(chunkIndex).getChunkNo()>novoChunk.getChunkNo()){
                        System.out.println("Almaceno chunk num " + chunkList.get(chunkIndex).getChunkNo() + " en posicion " + chunkIndex);
                        chunkList.add((chunkIndex-1),novoChunk);
                    }
                    else{}//ES el mismo chunk, no hay que añadirlo
                }
            
        }
        System.out.println(tamanio);
        System.out.println(chunkList.size());
        if (chunkList.size()==tamanio){
            
            //ordenar al peer juntar chunks
            fh  = new FileHandler();
            while(!(chunkList.isEmpty())){
                System.out.println("-------------------------");
                System.out.println(tamanio);
                System.out.println(chunkList.size());
                System.out.println("-------------------------");
                Chunk aux=chunkList.removeFirst();
                System.out.print(aux);
                fh.addChunk(aux.getFileId(),aux.getChunkNo(),aux.getChunk());
            }
            fh.makeFile();
            System.out.println("TERMINOOOO");
            tamanio=0;
            System.out.println(tamanio);
        }
    }
}
