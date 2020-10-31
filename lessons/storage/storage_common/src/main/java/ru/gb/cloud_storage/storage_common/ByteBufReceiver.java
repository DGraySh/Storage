package ru.gb.cloud_storage.storage_common;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ByteBufReceiver {

    private ByteBufReceiver() {
        throw new IllegalStateException("Utility class");
    }

    public static String receiveFileName(ByteBuf buf, State currentState) {
        byte[] fileName = null;
        int nameLength = 0;
        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
            nameLength = buf.readInt();
            System.out.printf("STATE: Get filename length = %s%n", nameLength );
            currentState = State.NAME;
        }

        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
            fileName = new byte[nameLength];
            buf.readBytes(fileName);
            System.out.printf("STATE: Filename received - %s%n", new String(fileName, StandardCharsets.UTF_8));
        }
        if (fileName != null) {
            return new String(fileName, StandardCharsets.UTF_8);
        } else
            throw new NullPointerException("File name is missing");
    }

    public static long receiveFileLength(ByteBuf buf, State currentState) {
        long length = 0L;
        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
            length = buf.readLong();
            System.out.printf("STATE: File length received - %s%n", length);
        }
        return length;
    }

    public static void receiveFile(ByteBuf buf, OutputStream out, long fileLength) throws IOException {
        long receivedFileLength = 0L;
        if (fileLength != 0) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    System.out.println("File received");
                    out.close();
                    break;
                    /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
                }
            }
        }
        else {
            System.out.println("Empty file received");
            out.close();
        }
    }
}
