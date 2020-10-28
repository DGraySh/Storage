package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.FileOperations;
import ru.gb.cloud_storage.storage_common.State;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class OperationTypeHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte read = buf.readByte();
                if (read == (byte) 20) receiveFile(buf);
                if (read > (byte) 30 && read < (byte)40) FileOpts(ctx.channel(), buf);
                if (read == (byte) 40) {
                    currentState = State.NAME_LENGTH;
                    Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
                    sendRequestedFile(buf, ctx.channel(), path, future -> {
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

    private void sendRequestedFile(ByteBuf buf, Channel channel, Path path, ChannelFutureListener finishListener) throws IOException {
        //Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
        ByteBufSender.sendFileOpt(channel, buf, (byte)45);
        ByteBufSender.sendFileName(channel, buf, path);
        currentState = State.NAME_LENGTH;
        System.out.println("STATE: Start file sending");
        ByteBufSender.sendFile(channel, buf, path, finishListener);
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        long receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("from_client_" + ByteBufReceiver.receiveFileName(buf, currentState)))) {
            currentState = State.FILE_LENGTH;
            long fileLength = ByteBufReceiver.receiveFileLength(buf, currentState);
            currentState = State.DATA;
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.IDLE;
                    System.out.println("File received");
                    break;
                    /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
                }
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

    private void deleteFile(Channel channel, ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
            ByteBufSender.sendFileOpt(channel, buf, (byte) 31);
            System.out.println("file " + path.getFileName() + " deleted by user _...");
            currentState = State.IDLE;
        } else {
            sendFileNotFound(channel, buf, path);
        }
    }

    private void sendFileNotFound(Channel channel, ByteBuf buf, Path path) {
        System.out.println("file not found");
        ByteBufSender.sendFileOpt(channel, buf, (byte) 0);
        ByteBufSender.sendFileName(channel, buf, path);//file not found
    }

    private void renameFile(Channel channel, ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        String fileName = ByteBufReceiver.receiveFileName(buf, currentState);
        Path path = Paths.get(fileName);
        currentState = State.NAME_LENGTH;
        String newFileName = ByteBufReceiver.receiveFileName(buf, currentState);
        if (Files.exists(path)) {
            Files.move(path, path.resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
            ByteBufSender.sendFileOpt(channel, buf, (byte) 32);
            System.out.println("file " + fileName + " renamed to " + newFileName + " by user _...");
            currentState = State.IDLE;
        } else {
            sendFileNotFound(channel, buf, path);
        }
    }

    private void moveFile(Channel channel, ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        String fileName = ByteBufReceiver.receiveFileName(buf, currentState);
        Path path = Paths.get(fileName);
        currentState = State.NAME_LENGTH;
        String newDir = ByteBufReceiver.receiveFileName(buf, currentState);
        if (Files.exists(path)) {
            Files.move(path, path.resolveSibling(newDir), StandardCopyOption.REPLACE_EXISTING);
            ByteBufSender.sendFileOpt(channel, buf, (byte) 33);
            System.out.println("file " + fileName + " moved to " + newDir + " by user _...");
            currentState = State.IDLE;
        } else {
            sendFileNotFound(channel, buf, path);
        }
    }

//    private long getFileLength(ByteBuf buf) {
//        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
//            fileLength = buf.readLong();
//            System.out.println("STATE: File length received - " + fileLength);
//            //currentState = State.DATA;
//        }
//        if (fileLength != 0)
//            return fileLength;
//        else
//            throw new NullPointerException("File name is missing");
//    }

//    private String getFileName(ByteBuf buf) {
//        byte[] fileName = null;
//        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
//            System.out.println("STATE: Get filename length");
//            nameLength = buf.readInt();
//            currentState = State.NAME;
//        }
//
//        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
//            fileName = new byte[nameLength];
//            buf.readBytes(fileName);
//            System.out.println("STATE: Filename received - _" + new String(fileName, StandardCharsets.UTF_8));
//        }
//        if (fileName != null) {
//            return new String(fileName);
//        } else
//            throw new NullPointerException("File name is missing");
//    }

    private void FileOpts(Channel channel, ByteBuf buf) throws IOException {
        currentState = State.OPTS;
        System.out.println("STATE: Start file operations");
        while (buf.readableBytes() > 0) {
            if (currentState == State.OPTS) {
                byte read = buf.readByte();
                if (read == (byte) 31) deleteFile(channel, buf);
                if (read == (byte) 32) renameFile(channel, buf);
                if (read == (byte) 33) moveFile(channel, buf);
            }
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}