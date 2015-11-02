package ru.rem.server.session.actions;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.packets.AbstractQuery;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

public class ResponseUpload extends AbstractQuery{
    
    public byte[] fileName;
    public int fileNameLength = 0;
    
    public ResponseUpload(PacketAction action){
        this.action = action;
    }
    
    @Override
    public void prepareFileData(ByteBuffer bb) {
        try {
            int val = getAmountElements();
            if (val < fileNameLength) {
                for (int p = val; bb.remaining() > 0 && (fileNameLength - p) != 0; p++) {
                    fileName[p] = bb.get();
                }
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(ActionFileListReader.class.getName()).log(Level.SEVERE, "", ex);
        }
    }

    @Override
    public int updateStatus(SocketChannel sc) {
        if(getDataProcessingResult() && getFileDataProcessingResult()){
            status = PacketStatus.COMPLETE;
        }
        
        if(!sc.isOpen() || !sc.isConnected()){
            status = PacketStatus.INTERRUPTED;
            return SelectionKey.OP_READ;
        }
        return 0;
    }

    public int getAmountElements() {
        if(this.fileName == null){
            return 0;
        }
        int count = 0;
        for (int b = 0; b < fileName.length; b++) {
            if (fileName[b] != 0) {
                count++;
            }
        }
        return count;
    }
    
    public int getRemainFileNameLength(int val){
        int difference = fileNameLength - val;
        return difference;
    }

    @Override
    public boolean getFileDataProcessingResult() {
        return getAmountElements() == fileNameLength;
    }

    @Override
    public void additionalData(ByteBuffer bb) {
        fileNameLength = bb.getInt();
        fileName = new byte[fileNameLength];
    }
    
    @Override
    public String toString(){
        return new String(fileName); 
    }

}
