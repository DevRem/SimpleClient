package ru.rem.server.session.assembly;

import java.nio.ByteBuffer;
import ru.rem.server.session.packets.AbstractPacket;

public abstract class PacketHandler {
    
    protected PacketHandler next;
    protected ServerNavigation serverChannel;
    
    public PacketHandler(ServerNavigation serverChannel){
        this.serverChannel = serverChannel;
    }
    
    public PacketHandler setNext(PacketHandler ph) {
        return next = ph;
    }
     
    public void handleRead(AbstractPacket packet, ByteBuffer bb) {
        
        read(packet, bb);
        if (next != null) {
            next.handleRead(packet, bb);
        }
        
    }
    
    public void handleWrite(AbstractPacket packet, ByteBuffer bb) {
       
        write(packet, bb);       
        if (next != null) {
            next.handleWrite(packet, bb);
        }
    }

    protected abstract void read(AbstractPacket packet, ByteBuffer bb);

    protected abstract void write(AbstractPacket packet, ByteBuffer bb);

}
