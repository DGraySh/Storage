package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ByteToMessageInboundHandler extends ReplayingDecoder<Byte[]> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {

        int length = buf.readInt();
        if (buf.readableBytes() < length) {
            return;
        }
        out.add(buf.readBytes(length));
    }
}
