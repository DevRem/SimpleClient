package ru.rem.server.session.actions;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.packets.AbstractResponse;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

public class ActionDownload extends AbstractResponse{

    public byte[] fileName;
    public int fileNameLength = 0;
    protected int fileNameCursor = 0;
    
    public ActionDownload(PacketStatus status, File file, String name) {
        this.status = status;
        this.action = PacketAction.DOWNLOAD;
        this.fileName = name.getBytes();
        this.fileNameLength = name.length();
        response = new ResponseDownload(this, file);
    }
    
    
    @Override
    public void prepareFileData(ByteBuffer bb) {
        try {
            for (; bb.hasRemaining() && fileNameCursor < fileName.length; fileNameCursor++) {
                bb.put(fileName[fileNameCursor]);
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(ActionFileListReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int updateStatus(SocketChannel sc) {
        
        if(getDataProcessingResult() && getFileDataProcessingResult()){
            status = PacketStatus.COMPLETE;
            return SelectionKey.OP_READ;
        }
       
        if(!sc.isOpen() || !sc.isConnected()){
            status = PacketStatus.INTERRUPTED;
        }
        return SelectionKey.OP_WRITE;
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
        bb.putInt(fileName.length);
    }
    
}

