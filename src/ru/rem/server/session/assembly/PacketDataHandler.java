package ru.rem.server.session.assembly;

import java.nio.ByteBuffer;
import ru.rem.server.session.packets.AbstractPacket;

public class PacketDataHandler extends PacketHandler{

    public PacketDataHandler(ServerNavigation serverChannel) {
        super(serverChannel);
    }
   
    @Override
    protected void read(AbstractPacket packet, ByteBuffer bb) {
        
        if(bb.remaining() > 0 && !packet.getDataProcessingResult()){
            packet.prepareData(bb);
        }
    }
    
   

    @Override
    protected void write(AbstractPacket packet, ByteBuffer bb) {
        
        if (!packet.getDataProcessingResult()) {
            packet.prepareData(bb);
        }
        
    }

    
}
