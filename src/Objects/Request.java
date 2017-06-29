package Objects;

import java.util.LinkedList;
import java.util.List;

public class Request
{
    private int id;
    private String date;
    private String pathName;
    private String file;
    private List<Integer> chunks;

    public Request(int id)
    {
        this.id = id;
        this.date = "";
        this.pathName = "";
        this.file = "";
        this.chunks = new LinkedList<>();
    }

    public Request(int id, String pathName, String file)
    {
        this.id = id;
        this.date = "";
        this.pathName = pathName;
        this.file = file;
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

    public List<Integer> getChunks() {
        return chunks;
    }

    public void setChunks(int chunkNo) {
        chunks.add(chunkNo);
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
