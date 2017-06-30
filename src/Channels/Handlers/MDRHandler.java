/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Channels.Handlers;

import Handlers.FileHandler;
import Objects.Chunk;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alba
 */
public class MDRHandler extends Thread
{

    private static LinkedList<Chunk> chunkList = new LinkedList<Chunk>();
    public static int tamanio = 0;
    public static String restoreLast = "";
    private final int peerID;
    private final DatagramPacket packet;
    private FileHandler fh;

    public MDRHandler(DatagramPacket packet, int peerID)
    {
        this.packet = packet;
        this.peerID = peerID;
    }

    @Override
    public void run()
    {
        String message = new String(packet.getData(), 0, packet.getLength());

        //Split the message into its components Header and Body.
        //What separates both of them are the <CRLF> characters.
        String[] msgComponents = message.split("\r\n\r\n");
        String msgHeader = msgComponents[0]; //SI ES NECESARIO, para el num de chunk y el file id

        //Create the chunk
        //0 - <MESSAGE_TYPE>
        //1 - <VERSION>
        //2 - <SENDER_ID>
        //3 - <FILE_ID>
        //4 - <CHUNK_NO>
        //5 - <"\r\n\r\n">
        //5 - <"\r\n\r\n">
        //5 - <CHUNK>
        String[] header = msgHeader.split(" ");

        //alternative for the loopback mode not working when peers
        //are running on the same machine. Compares the sender ID with
        //the receiver ID. If it's a match, ignore the package.
        if ((header[2]).equals(Integer.toString(Peer.Peer.getPeerID())))
        {
            return;
        }

        System.out.println("------------------------------------------------------");
        System.out.println("Message received: \n" + msgHeader);
        System.out.println("------------------------------------------------------\n");

        switch (header[0])
        {
            case "CHUNK":

                byte[] chunk;
                if (msgComponents.length > 2)
                {
                    chunk = msgComponents[2].getBytes();
                } else
                {
                    chunk = ("".getBytes());
                }

                Chunk novoChunk = new Chunk(header[3], Integer.parseInt(header[4]), chunk);

                try
                {
                    sleep(400);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(MDRHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("vuelvo al peer");
                System.out.println("ENTROOOOO");
                boolean added = false;
                System.out.println(chunkList.size());
                int chunkIndex = novoChunk.getChunkNo();
                System.out.println("Chunk num " + novoChunk.getChunkNo() + " INdex " + chunkIndex);
                if (chunkList.isEmpty())
                {
                    chunkList.add(novoChunk);
                    System.out.println("Almaceno chunk num " + novoChunk.getChunkNo() + " en posicion " + chunkIndex);
                } else
                {
                    //Aniade (o no, si ya estaba) el chunk a la lista
                    if ((chunkList.size() - 1) < chunkIndex)
                    {
                        System.out.println("ENTRO EN PRIMER IF ");
                        chunkIndex = 0;
                        while (chunkIndex < chunkList.size() && !added)
                        {
                            if (chunkList.get(chunkIndex).getChunkNo() == novoChunk.getChunkNo())
                            {
                                //nada
                                added = true;
                            } else if (chunkList.get(chunkIndex).getChunkNo() < novoChunk.getChunkNo())
                            {
                                chunkIndex++;
                            } else
                            {
                                chunkList.add((chunkIndex), novoChunk);
                                added = true;
                            }
                        }
                        if (!added)
                        {
                            chunkList.addLast(novoChunk);
                            added = true;
                        }
                    } else
                    { //chunkIndex == o < que chunk.size()-1

                        System.out.println("ENTRO EN SEGUNDO IF ");
                        if (chunkList.get(chunkIndex).getChunkNo() == novoChunk.getChunkNo())
                        {
                            //no hacer nada, ese chunk ya esta en la list
                            added = true;
                        } else if (chunkList.get(chunkIndex).getChunkNo() > novoChunk.getChunkNo())
                        {
                            System.out.println("POSICION MENOS ");
                            System.out.println("INDEX " + chunkIndex);
                            while (chunkList.get(chunkIndex).getChunkNo() > novoChunk.getChunkNo() && !added)
                            {
                                if (chunkIndex - 1 > 0)
                                {
                                    chunkIndex--;
                                } else
                                {
                                    chunkList.addFirst(novoChunk);
                                    added = true;
                                }
                                //Retroceder hasta encontrar uno menor o el mismo
                            }

                            if (!added)
                            {
                                if (chunkList.get(chunkIndex).getChunkNo() == novoChunk.getChunkNo())
                                {
                                    //no hacer nada, ese chunk ya esta en la list
                                    added = true;
                                } else
                                {//novochunk>index
                                    chunkList.add((chunkIndex + 1), novoChunk);
                                    added = true;
                                }
                            }
                        } else
                        {
                            //no se da este caso nunca
                        }

                    }
                }

                System.out.println(tamanio);
                System.out.println(chunkList.size());
                if (chunkList.size() == tamanio)
                {

                    //ordenar al peer juntar chunks
                    fh = new FileHandler();
                    while (!(chunkList.isEmpty()))
                    {
                        System.out.println("-------------------------");
                        System.out.println(tamanio);
                        System.out.println(chunkList.size());
                        System.out.println("-------------------------");
                        Chunk aux = chunkList.removeFirst();
                        System.out.print(aux);
                        fh.addChunk(aux.getFileId(), aux.getChunkNo(), aux.getChunk());
                    }
                    fh.makeFile();
                    System.out.println("TERMINOOOO");
                    tamanio = 0;
                    System.out.println(tamanio);
                }
        }
    }

}
