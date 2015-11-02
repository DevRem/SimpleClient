package ru.rem.server.session.packets;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

public abstract class AbstractQuery extends AbstractPacket{

    protected boolean dataReceived;

    @Override
    public void prepareData(ByteBuffer bb) {
	try{
            byte data = bb.get();
            status = PacketStatus.getById(data);
            action = PacketAction.getById(data);
            additionalData(bb);
            dataReceived = true;
	}catch(RuntimeException ex){
            Logger.getLogger(AbstractQuery.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Override
    public boolean getDataProcessingResult() {
        return dataReceived;
    }
    
    public abstract void additionalData(ByteBuffer bb);
}
