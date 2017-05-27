package Peer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomFileSystem
{
    String root;
    String encoded;
    
    public CustomFileSystem()
    {
        File dir = new File("FileSystem");
        String path = Paths.get(".").toAbsolutePath().normalize().toString() + "\\FileSystem";
        Path p = Paths.get(path);
        
        if(!(Files.exists(p)))
        {
            dir.mkdir();
        }
        
        root = path + "\\";
    }
    
    public String newDir(String dirName)
    {
        String dirPath = root + dirName;
        Path filePath = Paths.get(dirPath);
        
        if(!(Files.exists(filePath)))
        {
            File dirFile = new File(dirPath);
            dirFile.mkdir();
        }
        
        return dirPath;
    }
    
    public void removeDir(String dirName)
    {
        File dirFile = new File(root + dirName);
        String dirPath = root + dirName;
        Path filePath = Paths.get(dirPath);
        
        if(Files.exists(filePath))
            dirFile.delete();
    }
}
