package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ByteBufReceiver {

    public static State currentState = State.IDLE;
//    private int nameLength;
//    private long fileLength;
//    private BufferedOutputStream out;
//    ChannelHandlerContext ctx;



    public static String receiveFileName(ByteBuf buf, State currentState) {
        byte[] fileName = null;
        int nameLength = 0;
        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
            nameLength = buf.readInt();
            System.out.println("STATE: Get filename length = " + nameLength );
            currentState = State.NAME;
        }

        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
            fileName = new byte[nameLength];
            buf.readBytes(fileName);
            System.out.println("STATE: Filename received - _" + new String(fileName, StandardCharsets.UTF_8));
            //out = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
            //currentState = State.FILE_LENGTH;
        }
        if (fileName != null) {
            return new String(fileName);
        } else
            throw new NullPointerException("File name is missing");
    }

    public static long receiveFileLength(ByteBuf buf, State currentState) {
        long length = 0L;
        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
            length = buf.readLong();
            System.out.println("STATE: File length received - " + length);
        }
        if (length != 0)
            return length;
        else
            throw new NullPointerException("File name is missing");
    }

    public static void receiveFile(ByteBuf buf, OutputStream out, long fileLength) throws IOException {
        long receivedFileLength = 0L;
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (fileLength == receivedFileLength) {
                currentState = State.IDLE;
                System.out.println("File received");
                out.close();
                break;
                /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
            }
        }
    }
}
