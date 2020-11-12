package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import sun.plugin2.jvm.RemoteJVMLauncher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ByteBufSender {

    private ByteBufSender() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendFileOpt(Channel channel, byte comByte) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(comByte);
        channel.writeAndFlush(buf);
    }

    public static void sendFileName(Channel channel, Path path) {
        byte[] filenameBytes = path.toString().getBytes(StandardCharsets.UTF_8);

        ChannelPromise promise = channel.newPromise();
        promise.addListener(future -> {
            if (promise.isSuccess()) {
                System.out.println("promise success");
        } else {
                System.out.println("promise false");
        }
    });

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf, promise);
    }

    public static void sendFileName(Channel channel, Path path, CallBackReceive cbr) {

        byte[] filenameBytes = path.toString().getBytes(StandardCharsets.UTF_8);

        ChannelPromise promise = channel.newPromise();
        promise.addListener(future -> {
            if (promise.isSuccess()) {
                System.out.println("promise success");
        } else {
                System.out.println("promise false");
        }
    });

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf, promise);
    }

    public static void sendFile(Channel channel, Path path, ChannelFutureListener finishListener) throws IOException {

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}

