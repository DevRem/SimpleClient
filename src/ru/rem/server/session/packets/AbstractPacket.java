package ru.rem.server.session.packets;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

public abstract class AbstractPacket {
    
    public PacketStatus status = PacketStatus.PROCESSED;
    public PacketAction action;
    public AbstractPacket response;

    public AbstractPacket getResponse() {
        return response;
    }
    
    public PacketStatus getStatus(){
        return status;
    }

    public  PacketAction getAction(){
        return action;
    }   
    
    public abstract int updateStatus(SocketChannel sc);

    public abstract void prepareData(ByteBuffer bb);

    public abstract void prepareFileData(ByteBuffer bb);

    public abstract boolean getDataProcessingResult();

    public abstract boolean getFileDataProcessingResult();
    
}
