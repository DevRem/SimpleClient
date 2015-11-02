package ru.rem.server.session.assembly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import ru.rem.server.session.packets.AbstractPacket;
public interface ServerNavigation {
 
    public int send(ByteBuffer bb);
    
    public SocketChannel getChannel(); 
    
    public ByteBuffer getBuffer();
    
    public void addPacket(AbstractPacket packet);
    
    public void close();
    
}
