package ru.rem.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import ru.rem.server.session.actions.ActionFileListReader;
import ru.rem.server.session.assembly.PacketDataFileHandler;
import ru.rem.server.session.assembly.PacketDataHandler;
import ru.rem.server.session.assembly.PacketFileHandler;
import ru.rem.server.session.assembly.PacketHandler;
import ru.rem.server.session.assembly.ServerNavigation;
import ru.rem.server.session.fileHandler.PacketFile;
import ru.rem.server.session.packets.AbstractPacket;
import ru.rem.server.session.packets.AbstractQuery;
import ru.rem.server.session.packets.AbstractResponse;
import ru.rem.server.session.packets.data.PacketAction;
import ru.rem.server.session.packets.data.PacketStatus;

// Переделать так что бы в каждом блоке присутствовал первый байт отвечающий за действие

public class ServerHandler implements Runnable, ServerNavigation{
   
    public final int PORT = 8888;
    public final Client client;
    public final PacketHandler packetHandler;
    public SocketChannel channel;
    public ByteBuffer buffer = ByteBuffer.allocate(256);
    public List<AbstractPacket> packets = Collections.synchronizedList(new ArrayList<AbstractPacket>());
    public BlockingQueue<AbstractPacket> packetsQueue = new LinkedBlockingQueue<>();
    
    public ServerHandler(Client client){
        this.client = client;
        packetHandler = new PacketDataHandler(this);
        PacketHandler packetFileDataHandler = packetHandler.setNext(new PacketDataFileHandler(this));
        packetFileDataHandler.setNext(new PacketFileHandler(this));
        
    }
    
    private void processSelectedKeys(Selector selector) throws IOException{
        Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
        while (itr.hasNext()) {
            SelectionKey key = itr.next();
            itr.remove();
            if (key.isConnectable()) {
                channel.finishConnect();  
                refresh();
                channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }else if(key.isReadable()){
                processRead(selector);
            }else if(key.isWritable()){
                processWrite(selector);
            }           
        }
    }
    
    public void processRead(Selector selector) throws IOException {

        if(channel.read(getBuffer()) < 1){
            System.out.println("сервер упал :(");
            //channel.close();
            return;
        }
        
        getBuffer().flip();
        AbstractPacket packet = getProcessingPacket(getLastQuery());
        int interestOps = 0;
        if (packet == null) {
            interestOps = recognizePacket();
        }else if(packet instanceof AbstractQuery){
            packetHandler.handleRead(packet, getBuffer());
            interestOps = packet.updateStatus(getChannel());
            notifyOfResult(packet);
        }
        setOps(selector, interestOps);
        getBuffer().clear();
    }

    public void processWrite(Selector selector) throws ClosedChannelException {
       
        AbstractPacket packet = getProcessingPacket(getLastQuery());
        int interestOps = 0;
        if (packet == null) {
            updatePacketQueue();
        }else if(packet instanceof AbstractResponse){
            packetHandler.handleWrite(packet, getBuffer());
            interestOps = packet.updateStatus(getChannel());
            notifyOfResult(packet);
        }
        setOps(selector, interestOps);
         
    }
    
    public int recognizePacket(){
        byte settings = getBuffer().get();
        AbstractPacket packet = determineFromAction(settings);
        int interestOps = 0;
        if(packet != null){
            addPacket(packet);
            getBuffer().rewind();
            packetHandler.handleRead(packet, getBuffer());
            interestOps = packet.updateStatus(getChannel());
        }
        return interestOps;
    }
    
    public AbstractPacket determineFromAction(byte settings){
        PacketAction action = PacketAction.getById(settings);
        AbstractPacket packet = null;
        switch (action) {
             case UPDATE:
                packet = new ActionFileListReader(action);
                break;
        }
        return packet;
    }
    
    @Override
    public SocketChannel getChannel(){
        return channel;
    }
    
    @Override
    public int send(ByteBuffer bb){ 
        try {
            bb.flip();
            return getChannel().write(bb);
        } catch (IOException ex) {
           // Проверить
        }
        return 0;
    }
    
    public void сonnect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if(isConnected()){
            return;
        }
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }
    
    @Override
    public void close(){
        if(isConnected()){
            try {
                channel.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, "Ошибка закрытия канала", ex);
            }
            refresh();           
        }
    }
    
    public synchronized void refresh(){
         client.refreshConn(isConnected());
    }

    public boolean isConnected(){
        return channel.isConnected() && channel.isOpen();
    }
    
    public void addPacketToQueue(AbstractPacket packet){
        packetsQueue.add(packet);
    }
    
    @Override
    public void addPacket(AbstractPacket packet){
        packets.add(packet);
    }
    
    public boolean removePacket(AbstractPacket packet){
        return packets.remove(packet);
    }
    
    @Override
    public void run() {
        Selector selector;
        try {
            channel.connect(new InetSocketAddress(InetAddress.getLocalHost(), PORT));
            selector = Selector.open();
            if(channel.keyFor(selector) == null)
                channel.register(selector, SelectionKey.OP_CONNECT);
            while (channel.isOpen()) {
                selector.select();
                processSelectedKeys(selector);
            }
            selector.close();
        } catch (IOException ex) {

        }
    }
    
    public AbstractPacket getLastQuery(){
        if(packets.isEmpty()){
            return null;
        }
        return packets.get(packets.size()-1);
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    public void notifyOfResult(AbstractPacket packet) {

        if (packet.getResponse() == null 
                && PacketStatus.COMPLETE.equals(packet.getStatus())) {
            updatePacketQueue();
           
            if (packet.getAction().equals(PacketAction.UPDATE)) {
                addElementServerFile(packet);
            }else if(packet.getAction().equals(PacketAction.UPLOAD)){
                addElementServerFile(packet);
                client.showMessage(packet.getAction().getActionName(packet.toString()));
            }else{
                client.showMessage(packet.getAction().getActionName(packet.toString()));
            }
            
        }else if (packet.getStatus().equals(PacketStatus.INTERRUPTED)) {
            client.showMessage("Ошибка обработки " + packet.toString());
        }
        
        if(packet instanceof PacketFile){
            PacketFile file = (PacketFile) packet;
            switch(packet.getAction()){
                case DOWNLOAD:
                    client.updatePrBar(file.getDownloadStatus());
                    break;
                case UPLOAD:
                    client.updatePrBar(file.getDownloadStatus());
                    break;
            }
        }
        
    }
    
    public void addElementServerFile(AbstractPacket packet) {
        SwingUtilities.invokeLater(() -> {
            client.addElementServerFile(packet);
        });
    }
    
    public void updatePacketQueue(){
        AbstractPacket nextPacket = packetsQueue.poll();
        if (nextPacket != null) {
            addPacket(nextPacket);
        }
    }
    
    public AbstractPacket getProcessingPacket(AbstractPacket packet){
        if (packet == null) {
            return null;
        }else if (PacketStatus.PROCESSED.equals(packet.getStatus())) {
            return packet;	
        } else {
            return getProcessingPacket(packet.getResponse());
        }
    }
    
    public void setOps(Selector selector, int interestOps){
         if (interestOps == 0) {
            interestOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        }
        try {
            channel.register(selector, interestOps);
        } catch (ClosedChannelException ex) {
            refresh();
        }
    }
    
}
