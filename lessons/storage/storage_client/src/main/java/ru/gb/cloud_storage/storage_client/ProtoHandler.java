package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.State;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readBytes = buf.readByte();
                if (readBytes == (byte) 45) receiveFile(buf);
                else if (readBytes == (byte) 31) System.out.println("file deleted");
                else if (readBytes == (byte) 32) System.out.println("file renamed");
                else if (readBytes == (byte) 33) System.out.println("file moved");
                else if (readBytes == (byte) 35) receiveFileTree(buf);
                else if (readBytes == (byte) 0) receiveFileNotFound(buf);
                else System.out.println("ERROR: Invalid first byte - " + readBytes);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);

        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("_from_Server_" + fileName));
        System.out.println("STATE: Start file receiving");
        ByteBufReceiver.receiveFile(buf, out, fileLength);
        //currentState = State.IDLE;
    }

//
//        ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(Paths.get("_from_Server_")));
//        DataInputStream in = new DataInputStream(bais);
//        ArrayList<Path> paths = new ArrayList<>();
//        while (in.available() > 0) {
//            String element = in.readUTF();
//            paths.add(Paths.get(element));
//        }
//        System.out.println(paths);


    private void receiveFileTree(ByteBuf buf) throws IOException {
        System.out.println("STATE: Start tree bytes receiving");
        byte[] bytes = new byte[buf.readableBytes()];

        while (buf.readableBytes() > 0) {
            buf.readBytes(bytes);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        ArrayList<Path> paths = new ArrayList<>();

        while (in.available() > 0) {
            String element = in.readUTF();
            paths.add(Paths.get(element));
        }

        System.out.println("Tree received");
        System.out.println(paths);
    }

    private void receiveFileNotFound(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        System.out.println("file "+ fileName + " not found");//TODO
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
