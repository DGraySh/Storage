package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.State;

import java.io.*;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
   // private BufferedOutputStream out;

    //FIRST_BYTE, INT , FILE_NAME, FILE_LEN, FILE_DATA
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 45) {
                    currentState = State.NAME_LENGTH;
                    String fileName = ByteBufReceiver.receiveFileName(buf, currentState);
                    currentState = State.FILE_LENGTH;
                    long fileLength = ByteBufReceiver.receiveFileLength(buf, currentState);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("_from_Server_" + fileName));
                    System.out.println("STATE: Start file receiving");
                    ByteBufReceiver.receiveFile(buf, out, fileLength);
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
                if (readed == (byte) 31) {
                    System.out.println("file deleted");
                }
                if (readed == (byte) 32) {
                    System.out.println("file renamed");
                }
                if (readed == (byte) 33) {
                    System.out.println("file moved");
                }
            }

//            if (currentState == State.NAME_LENGTH)
//                ByteBufReceiver.receiveFileName(buf);
//
//            if (currentState == State.FILE_LENGTH)
//                ByteBufReceiver.receiveFileLength(buf);
//
//            if (currentState == State.DATA)
//                ByteBufReceiver.receiveFile(buf, out, fileLength);
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
