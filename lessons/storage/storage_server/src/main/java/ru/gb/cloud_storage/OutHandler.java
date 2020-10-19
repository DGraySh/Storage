package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Path path = Paths.get((String) msg);

//        byte filenameBytes = (byte) msg;
//        byte[] arr = str.getBytes();
//        ByteBuf buf = ctx.alloc().buffer(8);
//        buf.writeBytes(arr);
//        ctx.writeAndFlush(buf);
        // buf.release();
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(((String) msg).getBytes().length);
        buf.writeBytes(((String) msg).getBytes());
        ctx.write(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));
        ctx.writeAndFlush(buf);
    }
}

