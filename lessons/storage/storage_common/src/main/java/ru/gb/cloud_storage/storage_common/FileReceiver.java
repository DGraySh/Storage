package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReceiver {
    private BufferedOutputStream out;

    public void receiveFile(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 20);
        channel.write(buf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.write(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.write(buf);

        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            System.out.println("File received");
            out.close();
            //break;
            /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
        }
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}
