package ru.rem.server.session.fileHandler;

import java.nio.channels.FileChannel;

public interface PacketFile {
   
    public void incPosition(long val);
    
    public long getPosition();
    
    public FileChannel getFileChannel();
    
    public boolean getFileProcessingResult();
    
    public int getDownloadStatus();
    
}
