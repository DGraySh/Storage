package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.lang3.ArrayUtils;

public class MessageToByteOutboundHandler extends MessageToByteEncoder<Byte[]> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Byte[] msg, ByteBuf out) {
        System.out.println("outbound init");
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        buf.writeInt(msg.length);
        buf.writeBytes(ArrayUtils.toPrimitive(msg));
        out.writeBytes(buf);
    }
}
