package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ByteBufSender {

    public static void sendFileOpt(Channel channel, ByteBuf buf, byte comByte) {
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(comByte);
        channel.writeAndFlush(buf);
    }

    public static void sendFileName( Channel channel, ByteBuf buf, Path path) throws IOException {
//        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeUTF(path.toString());

        byte[] pathNameBytes = baos.toByteArray();
        buf.writeBytes(pathNameBytes);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(pathNameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(pathNameBytes.length);
        buf.writeBytes(pathNameBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendFile(Channel channel, ByteBuf buf, Path path, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}

