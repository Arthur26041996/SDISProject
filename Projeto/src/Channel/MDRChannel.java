package Channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MDRChannel implements Runnable
{
    private String IP;
    private int PORT;
    private InetAddress group;
    private MulticastSocket mcst;

    public MDRChannel(String IP, int PORT) throws UnknownHostException, IOException
    {
        this.IP = IP;
        this.PORT = PORT;
        group = InetAddress.getByName(IP);
        mcst = new MulticastSocket(PORT);
    }
    
    @Override
    public void run()
    {
        try
        {
            mcst.joinGroup(group);
        }
        catch (IOException ex)
        {
            
        }
    }
    
}
