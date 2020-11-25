package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ByteBufSender {

    private ByteBufSender() {
        throw new IllegalStateException("Utility class");
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

    public static ByteBuf getByteBufToSend(byte[]... args) {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (byte[] b : args) {
            try {
                output.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] b = output.toByteArray();

        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buf.writeInt(b.length);
        return buf.writeBytes(b);
    }


    public static byte[] getFileNameBytes(Path path) throws IOException {

        byte[] filenameBytes = path.toRealPath().toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(filenameBytes.length);
        output.write(filenameBytes);

        byte[] b = output.toByteArray();
        output.close();

        return b;
    }
}

/*
        public static void sendFileOpt(ChannelHandlerContext ctx, byte comByte, ChannelFutureListener listener) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(comByte);
            ctx.writeAndFlush(buf).addListener(listener);
        }
    */
/*
    public static void sendFileOpt(Channel channel, byte comByte) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(comByte);
        channel.writeAndFlush(buf)*/
/* ChannelFuture future = channel.writeAndFlush(Unpooled.copiedBuffer(buf)).addListener(ChannelFutureListener.CLOSE);
        try {
            future.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
/*
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
        System.out.println("Send length = " + filenameBytes.length);
        channel.writeAndFlush(buf).addListener(future -> {
            if (future.isDone()) {
                ByteBuf anotherBuf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
                anotherBuf.writeBytes(filenameBytes);
                System.out.println("Send filename = " + Arrays.toString(filenameBytes));
                channel.writeAndFlush(anotherBuf, promise);
            } else
                System.out.println("Error in sending filename");
        });


        */
/*buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        System.out.println("Send filename = " + Arrays.toString(filenameBytes));
        channel.writeAndFlush(buf, promise);*/
/*
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
*/
