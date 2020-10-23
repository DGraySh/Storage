package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.State;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OperationTypeHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int nameLength;
    private long fileLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte read = buf.readByte();
                if (read == (byte) 20) receiveFile(buf);
                //if (buf.readByte() == (byte) 30) FileOpts(buf);
                if (read == (byte) 40) {
                    currentState = State.NAME_LENGTH;
                    Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
                    sendReqFile(buf, ctx.channel(), path, future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                        if (future.isSuccess()) {
                            System.out.println("Файл успешно передан");
                        }
                    });

                }
                if (buf.readableBytes() == 0) {
                    buf.release();
                }
            }
        }
    }

    private void sendReqFile(ByteBuf buf, Channel channel, Path path, ChannelFutureListener finishListener) throws IOException {
        //Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
        ByteBufSender.sendFileOpt(channel, buf, (byte)45);
        ByteBufSender.sendFileName(channel, buf, path);
        currentState = State.NAME_LENGTH;
        System.out.println("STATE: Start file sending");
//        ByteBufSender.sendFileName(channel, buf, path);
        ByteBufSender.sendFile(channel, buf, path, finishListener);
    }


    private void receiveFile(ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        long receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("from_client_" + getFileName(buf)));
        //getFileName(buf);
        currentState = State.FILE_LENGTH;
        getFileLength(buf);
        currentState = State.DATA;
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (getFileLength(buf) == receivedFileLength) {
                currentState = State.IDLE;
                System.out.println("File received");
                out.close();
                break;
                /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
            }
        }
    }

    private void sendFile(String fileName, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Paths.get(fileName);

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBuf buf = null;
        ByteBufSender.sendFileOpt(channel, buf, (byte) 20);
        ByteBufSender.sendFileName(channel, buf, path);
        ByteBufSender.sendFile(channel, buf, path, finishListener);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private long getFileLength(ByteBuf buf) {
        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
            fileLength = buf.readLong();
            System.out.println("STATE: File length received - " + fileLength);
            //currentState = State.DATA;
        }
        if (fileLength != 0)
            return fileLength;
        else
            throw new NullPointerException("File name is missing");
    }

    private String getFileName(ByteBuf buf) throws FileNotFoundException {
        byte[] fileName = null;
        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
            System.out.println("STATE: Get filename length");
            nameLength = buf.readInt();
            currentState = State.NAME;
        }

        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
            fileName = new byte[nameLength];
            buf.readBytes(fileName);
            System.out.println("STATE: Filename received - _" + new String(fileName, StandardCharsets.UTF_8));
//            out = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
            //currentState = State.FILE_LENGTH;
        }
        if (fileName != null) {
            return new String(fileName);
        } else
            throw new NullPointerException("File name is missing");
    }

//    private void FileOpts(ByteBuf buf) throws IOException {
//        currentState = State.OPTS;
//        System.out.println("STATE: Start file operations");
//        while (buf.readableBytes() > 0) {
//            if (currentState == State.OPTS) {
//                if (buf.readByte() == (byte) 31) DeleteFile(buf);
//                if (buf.readByte() == (byte) 32) RenameFile(buf);
//                if (buf.readByte() == (byte) 33) MoveFile(buf);
//            }
//        }
//    }
//
//    private void deleteFile(ByteBuf buf) throws IOException {
//    }//TODO
//
//    private void renameFile(ByteBuf buf) throws IOException {
//    }//TODO
//
//    private void moveFile(ByteBuf buf) throws IOException {
//    }//TODO

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}