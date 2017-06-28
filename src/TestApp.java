

import Peer.RemoteInterface;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp
{   
    public static void main(String[] args) throws UnknownHostException
    {
        if(args.length < 2 || args.length > 4)
        {
            System.out.println("[TEST APP]: INVALID USAGE"
                             + "\nUSAGE: <PEER AP> <MESSAGE TYPE> <OPERANDS>*"
                             + "\n<PEER AP>: PEER ACCESS POINT"
                             + "\n<MESSAGE TYPE>: \'BACKUP\', \'RESTORE\', \'DELETE\', \'RECLAIM\' OR \'STATE\'"
                             + "\n<OPERANDS>:"
                             + "\n\t[BACKUP]: <FILE PATH> <DESIRED REPLICATION DEGREE>"
                             + "\n\t[RESTORE]: ???"
                             + "\n\t[DELETE]: <FILE NAME>"
                             + "\n\t[RECLAIM]: <VALUE TO RECLAIM IN Kb, WHERE K STANDS FOR 1000>"
                             + "\n\t[STATE] - NO OPERANDS - ");
            return;
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
                    ri.delete(args[2]);
                    break;
                    
                case "RECLAIM":
                    ri.reclaim(Long.parseLong(args[2]));
                    break;
                    
                case "STATE":
                    StringBuilder sb = ri.state();
                    System.out.println(sb);
                    break;
                    
                default:
                    System.out.println("Invalid arguments!\n"
                                    + "Valid arguments: \'BACKUP\', \'RESTORE\', \'DELETE\', \'RECLAIM\' or \'STATE\'");
                    break;
            }
        }
        catch (Exception ex)
        {
            System.out.println("[TESTAPP]: REMOTE COMMUNICATION WITH REGISTRY FAILED");
            ex.printStackTrace();
        }
    }
}
