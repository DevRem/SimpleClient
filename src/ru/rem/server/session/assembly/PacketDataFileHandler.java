package ru.rem.server.session.assembly;

import java.io.IOException;
import java.nio.ByteBuffer;
import ru.rem.server.session.packets.AbstractPacket;

public class PacketDataFileHandler extends PacketHandler{

    public PacketDataFileHandler(ServerNavigation serverChannel) {
        super(serverChannel);
    }
    
    @Override
    protected void read(AbstractPacket packet, ByteBuffer bb) {
        
        if(bb.remaining() > 0 && !packet.getFileDataProcessingResult()){
            packet.prepareFileData(bb);
        }
    }
      
    @Override
    protected void write(AbstractPacket packet, ByteBuffer bb){
        
        if (!packet.getFileDataProcessingResult()) {
            packet.prepareFileData(bb);
        }
        try {
            if (bb.position() > 0) {
                bb.flip();
                serverChannel.getChannel().write(bb);
                bb.clear();
            }
        } catch (IOException ex) {
            serverChannel.close();
        }

    }

}
