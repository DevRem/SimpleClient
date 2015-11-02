package ru.rem.server.session.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.fileHandler.PacketFile;
import ru.rem.server.session.packets.AbstractResponse;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;


public class ActionUpload extends AbstractResponse implements PacketFile{

    protected byte[] fileName;
    protected int fileNameCursor = 0;
    protected FileChannel fileHandler;
    protected long fileSize;
    protected long fileCursor;
    
    public ActionUpload(PacketStatus status, File file) {
        this.status = status;
        this.action = PacketAction.UPLOAD;
        try {
            this.fileHandler = new FileInputStream(file).getChannel();
            this.fileName = file.getName().getBytes();
            this.fileSize = file.length();
            response = new ResponseUpload(action);
        } catch (IOException ex) {
           Logger.getLogger(ActionUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Override
    public void prepareFileData(ByteBuffer bb) {
        try {
            for (; bb.hasRemaining() && fileNameCursor < fileName.length; fileNameCursor++) {
                bb.put(fileName[fileNameCursor]);
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(ActionUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int updateStatus(SocketChannel sc) {
        
        if(getDataProcessingResult() && getFileDataProcessingResult() && getFileProcessingResult()){
            status = PacketStatus.COMPLETE;
            return SelectionKey.OP_READ;
        }
        
        if(!sc.isOpen() || !sc.isConnected()){
            status = PacketStatus.INTERRUPTED;
        }
        return SelectionKey.OP_WRITE;
    }
    
    @Override
    public void incPosition(long val) {
        fileCursor += val;
    }

    @Override
    public long getPosition() {
        return fileCursor;
    }

    @Override
    public FileChannel getFileChannel() {
        return fileHandler;
    }

    @Override
    public boolean getFileProcessingResult() {
        return fileCursor == fileSize ;
    }


    @Override
    public boolean getFileDataProcessingResult() {
        return fileNameCursor == fileName.length;
    }
    
    @Override
    public String toString(){
        return new String(fileName); 
    }

    @Override
    public void additionalData(ByteBuffer bb) {
        if (fileNameCursor == 0) {
            bb.putInt(fileName.length);
            bb.putLong(fileSize);
        }
    }
    
    @Override
    public int getDownloadStatus() {
        return (int) ((float) fileCursor / fileSize * 100);
    }
    
}
