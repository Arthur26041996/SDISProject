package Peer;

public class RepDegree
{
    private String fileName;
    private int repDegDesired;
    public int repDegActual;

    public RepDegree(String fileName, int repDegDesired, int repDegActual)
    {
        this.fileName = fileName;
        this.repDegDesired = repDegDesired;
        this.repDegActual = repDegActual;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public int getRepDegDesired()
    {
        return repDegDesired;
    }

    public void setRepDegDesired(int repDegDesired)
    {
        this.repDegDesired = repDegDesired;
    }

    public int getRepDegActual()
    {
        return repDegActual;
    }

    public void setRepDegActual(int repDegActual)
    {
        this.repDegActual = repDegActual;
    }

    @Override
    public boolean equals(Object obj)
    {
        return fileName.equals(((RepDegree)obj).fileName);
    }
    
    
}
