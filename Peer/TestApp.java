package Peer;

import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp
{   
    public static void main(String[] args) throws UnknownHostException
    {
        if(args.length < 3)
        {
            
            System.out.println("Error: too few arguments!!\n"
                             + "Usage: <peer_ap> <operation> <opnd>*");
        }
        
        try
        {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            RemoteInterface ri = (RemoteInterface) registry.lookup(args[0]);
            
            switch(args[1])
            {
                case "BACKUP":
                    ri.backUp(args[2], Integer.parseInt(args[3]));
                    break;
                    
                case "RESTORE":
                    ri.restore("teste");
                    break;
                    
                case "DELETE":
                    break;
                    
                case "RECLAIM":
                    ri.reclaim(5);
                    break;
                    
                case "STATE":
                    break;
                    
                default:
                    System.out.println("Invalid arguments!\n"
                                    + "Valid arguments: \'BACKUP\', \'RESTORE\', \'DELETE\', \'RECLAIM\' or \'STATE\'");
            }
        }
        catch (Exception ex)
        {
            System.out.println("[TESTAPP]: "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
