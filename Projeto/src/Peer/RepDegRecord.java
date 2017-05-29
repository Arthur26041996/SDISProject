package Peer;

import java.util.LinkedList;

public class RepDegRecord
{
    private LinkedList<RepDegree> rep;
    
    public RepDegRecord()
    {
        rep = new LinkedList<>();
    }
    
    public void addRecord(RepDegree record)
    {
        rep.add(record);
    }
    
    public void addRecord(String fileName, int desiredRepDeg, int actualRepDeg)
    {
        rep.add(new RepDegree(fileName, desiredRepDeg, actualRepDeg));
    }
    
    public void setDesiredRepDegree(String fileName, int desiredRepDeg)
    {
        for(RepDegree r : rep)
            if(r.getFileName().equals(fileName))
                r.setRepDegDesired(desiredRepDeg);
    }
    
    public int getDesiredRepDegree(String fileName)
    {
        for(RepDegree r : rep)
            if(r.getFileName().equals(fileName))
                return r.getRepDegDesired();
        
        return -1;
    }
    
    public void setActualRepDegree(String fileName, int actualRepDeg)
    {
        for(RepDegree r : rep)
            if(r.getFileName().equals(fileName))
                r.setRepDegActual(actualRepDeg);
    }
    
    public void increaseActualRepDegree(String fileName)
    {
        for(RepDegree r : rep)
            if(r.getFileName().equals(fileName))
                r.repDegActual++;
    }
    
    public int getActualRepDegree(String fileName)
    {
        for(RepDegree r : rep)
            if(r.getFileName().equals(fileName))
                return r.getRepDegActual();
        
        return -1;
    }
}
