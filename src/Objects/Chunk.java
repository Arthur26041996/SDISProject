package Objects;

public class Chunk
{
    public int ChunkNo;
    public String FileId;
    public byte[] Chunk;
    
    public Chunk(String fileId, int chunkNo, byte[] chunk)
    {
        this.FileId = fileId;
        this.ChunkNo = chunkNo;
        this.Chunk = chunk;
    }

    public int getChunkNo()
    {
        return ChunkNo;
    }

    public void setChunkNo(int ChunkNo)
    {
        this.ChunkNo = ChunkNo;
    }

    public String getFileId()
    {
        return FileId;
    }

    public void setFileId(String FileId)
    {
        this.FileId = FileId;
    }

    public byte[] getChunk()
    {
        return Chunk;
    }

    public void setChunk(byte[] Chunk)
    {
        this.Chunk = Chunk;
    }
}
