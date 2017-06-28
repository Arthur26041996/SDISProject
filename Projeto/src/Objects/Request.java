package Objects;

import java.util.HashMap;
import java.util.Map;

public class Request
{
    private int id;
    private String date;
    private String pathName;
    private String file;
    private int desiredRD;
    private Map<Integer, Integer> chunks;

    public Request(int id)
    {
        this.id = id;
        this.date = "";
        this.pathName = "";
        this.file = "";
        this.desiredRD = 0;
        this.chunks = new HashMap<>();
    }

    public Request(int id, String pathName, String file, int desiredRD, int chunkNo, int chunkRD)
    {
        this.id = id;
        this.date = "";
        this.pathName = pathName;
        this.file = file;
        this.desiredRD = desiredRD;
        this.chunks = new HashMap<>();
        chunks.put(chunkNo, chunkRD);
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
    
    public String getPathName()
    {
        return pathName;
    }

    public void setPathName(String pathName)
    {
        this.pathName = pathName;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public int getDesiredRD()
    {
        return desiredRD;
    }

    public void setDesiredRD(int desiredRD)
    {
        this.desiredRD = desiredRD;
    }

    public Map<Integer, Integer> getChunks()
    {
        return chunks;
    }

    public void setChunks(Map<Integer, Integer> chunks)
    {
        this.chunks = chunks;
    }
    
    public void setChunks(int chunkNo, int chunkRD)
    {
        this.chunks.put(chunkNo, chunkRD);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Request other = (Request) obj;
        if (this.id != other.id)
        {
            return false;
        }
        return true;
    }
}
