package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomFileSystem
{
    String root;
    
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
    
    public String newSubDir(String dirPath, String subDirName)
    {
        if(Files.exists(Paths.get(dirPath)))
        {
            String path = dirPath + "\\" + subDirName;
            
            if(!(Files.exists(Paths.get(path))))
            {
                File dirFile = new File(path);
                dirFile.mkdir();
            }
            
            return path;
        }
        else
        {
            File file = new File(dirPath);
            return newSubDir(newDir(file.getName()), subDirName);
        }
    }
    
    public boolean removeDir(File dirName)
    {
        File[] files = dirName.listFiles();
        
        if(files != null)
        {
            for(File f : files)
            {
                if(f.isDirectory())
                    removeDir(f);
                else
                    f.delete();
            }
            dirName.delete();
            return true;
        }
        return false;
    }

    public String createControlDir(int peerId)
    {
        String dirPath = root + "Peer_" + peerId + "\\Control";
        Path path = Paths.get(dirPath);
        
        if(!(Files.exists(path)))
        {
            File controlDir = new File(dirPath);
            controlDir.mkdir();
        }
        
        return dirPath;
    }
}
