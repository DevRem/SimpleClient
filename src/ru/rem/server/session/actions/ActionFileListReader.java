package ru.rem.server.session.actions;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.packets.AbstractQuery;
import ru.rem.server.session.packets.AbstractResponse;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

public class ActionFileListReader extends AbstractQuery{

    protected byte[] fileName = null;
    protected int index = -1;
    protected int length;
    protected long size;

    public ActionFileListReader(PacketAction action) {
        this.action = action;
        response = new ResponseUpdateFileList(action);
    }

    @Override
    public boolean getFileDataProcessingResult() {
        return getAmountElements() == length;
    }

    @Override
    public void prepareFileData(ByteBuffer bb) {
        
        try {
            int val = getAmountElements();
            if (val < length) {
                for (int p = val; bb.remaining() > 0 && (length - p) != 0; p++) {
                    fileName[p] = bb.get();
                }
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(ActionFileListReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getAmountElements() {
        if (this.fileName == null) {
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
    
    @Override
    public int updateStatus(SocketChannel sc) {
        if (getDataProcessingResult() && getFileDataProcessingResult()) {
            status = PacketStatus.COMPLETE;
            return SelectionKey.OP_WRITE;
        } else if (!sc.isOpen() || !sc.isConnected()) {
            status = PacketStatus.INTERRUPTED;
        }
        return SelectionKey.OP_READ;
    }

    @Override
    public void additionalData(ByteBuffer bb) {
        length = bb.getInt();
        size = bb.getLong();
        index = bb.getInt();
        fileName = new byte[length];
    }

    
    public class ResponseUpdateFileList extends AbstractResponse {

        public boolean indexSent = false;

        public ResponseUpdateFileList(PacketAction action){
            this.action = action;
        }
        
        @Override
        public void prepareFileData(ByteBuffer bb) {
            try {
                bb.putInt(index);
                indexSent = true;
            } catch (RuntimeException ex) {
                Logger.getLogger(ResponseUpdateFileList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public int updateStatus(SocketChannel sc) {

            if (this.getDataProcessingResult() && this.getFileDataProcessingResult()) {
                this.status = PacketStatus.COMPLETE;
                if (index == 0) {
                    return 0;
                } else {
                    return SelectionKey.OP_READ;
                }
            } 
            if (!sc.isOpen() || !sc.isConnected()) {
                this.status = PacketStatus.INTERRUPTED;
                return SelectionKey.OP_READ;
            }
            return SelectionKey.OP_WRITE;
        }

        @Override
        public boolean getFileDataProcessingResult() {
            return indexSent;
        }

        @Override
        public String toString() {
            return new String(fileName);
        }

        @Override
        public void additionalData(ByteBuffer bb) {
            
        }
        
    }
    
}

