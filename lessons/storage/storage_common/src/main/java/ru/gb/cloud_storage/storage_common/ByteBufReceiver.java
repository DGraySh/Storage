package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteBufReceiver {

    private ByteBufReceiver() {
        throw new IllegalStateException("Utility class");
    }

    public static String receiveFileName(Channel channel, ByteBuf buf, State currentState) {

        byte[] fileName = null;
        int nameLength = 0;
        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
            nameLength = buf.readInt();
            System.out.println("length " + nameLength);
            currentState = State.NAME;
            if (nameLength != 0)
                ByteBufSender.sendFileOpt(channel, (byte) 20);
            else
                ByteBufSender.sendFileOpt(channel, (byte) 22);
        }

        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
            fileName = new byte[nameLength];
            buf.readBytes(fileName);
            System.out.println("filename " + Arrays.toString(fileName));
        }
        if (fileName != null) {
            return new String(fileName, StandardCharsets.UTF_8);
        } else {
            throw new NullPointerException("File name is missing");
        }
    }

    public static long receiveFileLength(ByteBuf buf, State currentState) {
        long length = 0L;
        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
            length = buf.readLong();
        }
        return length;
    }

    public static void receiveFile(ByteBuf buf, OutputStream out, long fileLength, Logger logger) throws IOException {
        long receivedFileLength = 0L;
        if (fileLength != 0) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    logger.info("File successfully received");
                    out.close();
                    break;
                    /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
                }
            }
        } else {
            logger.warn("Empty file received");
            out.close();
        }
    }
}
