package ru.rem.server.session.assembly;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rem.server.session.actions.ActionFileListReader;
import ru.rem.server.session.fileHandler.PacketFile;
import ru.rem.server.session.packets.AbstractPacket;

public class PacketFileHandler extends PacketHandler {

    public PacketFileHandler(ServerNavigation serverChannel) {
        super(serverChannel);
    }

    @Override
    protected void read(AbstractPacket packet, ByteBuffer bb) {
        if ((packet instanceof PacketFile) && bb.remaining() > 0) {
            try {
                PacketFile fileHandler = (PacketFile) packet;
                FileChannel fc = fileHandler.getFileChannel();
                long written = fc.write(bb);
                fileHandler.incPosition(written);
                if (fileHandler.getFileProcessingResult()) {
                    close(fc);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                // Не получилось запись файл 
            }
        }
    }

    @Override
    protected void write(AbstractPacket packet, ByteBuffer bb) {

        try {
            if ((packet instanceof PacketFile) && packet.getFileDataProcessingResult()) {
                PacketFile fileHandler = (PacketFile) packet;
                if (!fileHandler.getFileProcessingResult()) {
                    FileChannel fc = fileHandler.getFileChannel();
                    if (fc.isOpen()) {
                        long written = fc.transferTo(fileHandler.getPosition(), bb.capacity(), serverChannel.getChannel());
                        fileHandler.incPosition(written);
                        if (fileHandler.getFileProcessingResult()) {
                            close(fc);
                        }
                    }
                }
            }
        } catch (IOException ex) {
             serverChannel.close();
        }
    }

    public void close(FileChannel channel) throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

}
