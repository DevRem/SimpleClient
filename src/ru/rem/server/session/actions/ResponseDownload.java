package ru.rem.server.session.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.fileHandler.PacketFile;
import ru.rem.server.session.packets.AbstractQuery;
import ru.rem.server.session.packets.AbstractResponse;
import ru.rem.server.session.packets.data.PacketStatus;

public class ResponseDownload extends AbstractQuery implements PacketFile {
    
    protected FileChannel fileHandler;
    protected byte[] fileName = null;
    protected int fileNameLength;
    protected int fileNameCursor;
    protected long fileLength;
    protected long position;

    public ResponseDownload(AbstractResponse query, File file) {
        this.action = query.getAction();
        try {
            this.fileHandler = new FileOutputStream(file).getChannel();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ActionFileListReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean getFileDataProcessingResult() {
        return getAmountElements() == fileNameLength;
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
        if (getDataProcessingResult() && getFileDataProcessingResult() && getFileProcessingResult()) {
            status = PacketStatus.COMPLETE;
            return 0;
        } 
        
        if (!sc.isOpen() || !sc.isConnected()) {
            status = PacketStatus.INTERRUPTED;
        }
        return SelectionKey.OP_READ;
    }

    @Override
    public void incPosition(long val) {
        position += val;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public FileChannel getFileChannel() {
        return fileHandler;
    }
    
    @Override
    public boolean getFileProcessingResult() {
        return getPosition() == fileLength;
    }

    @Override
    public void additionalData(ByteBuffer bb) {
        fileNameLength = bb.getInt();
        fileLength = bb.getLong();
        fileName = new byte[fileNameLength];
    }
    
    @Override
    public String toString(){
        return new String(fileName); 
    }

    @Override
    public int getDownloadStatus() {
        return (int) ((float) position / fileLength * 100);
    }
    
}
