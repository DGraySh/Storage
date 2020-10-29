package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.State;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class OperationTypeHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;

    private static void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
        }
        if (future.isSuccess()) {
            System.out.println("Файл успешно передан");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readBytes = buf.readByte();
                if (readBytes == (byte) 20) receiveFile(buf);
                if ((readBytes > (byte) 30 ) && (readBytes < (byte)40)) FileOpts(ctx.channel(), buf, readBytes);
                if (readBytes == (byte) 40) sendRequestedFile(buf, ctx.channel(), OperationTypeHandler::operationComplete);
                if (buf.readableBytes() == 0) {
                    buf.release();
                }
            }
        }
    }

    private void sendFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        sendRequestedFile(buf, ctx.channel(), OperationTypeHandler::operationComplete);
    }

    private void FileOpts(Channel channel, ByteBuf buf, byte read) throws IOException {
        currentState = State.OPTS;
        System.out.println("STATE: Start file operations");
        if (read == (byte) 31) deleteFile(channel, buf);
        if (read == (byte) 32) renameFile(channel, buf);
        if (read == (byte) 33) moveFile(channel, buf);
        if (read == (byte) 35) sendWalkTree(channel, buf, OperationTypeHandler::operationComplete);

    }

    private void sendRequestedFile(ByteBuf buf, Channel channel, ChannelFutureListener finishListener) throws IOException {
        currentState = State.NAME_LENGTH;
        Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
        if (Files.exists(path)) {
            ByteBufSender.sendFileOpt(channel, buf, (byte) 45);
            ByteBufSender.sendFileName(channel, buf, path);
            System.out.println("STATE: Start requested file sending");
            ByteBufSender.sendFile(channel, buf, path, finishListener);
        } else {
            sendFileNotFound(channel, buf, path);
            System.out.println("file "+ path.getFileName() + " not found");//TODO
        }
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        long receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("from_client_" + ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH)))) {
            long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
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

    private void deleteFile(Channel channel, ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, currentState));
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
            System.out.println("file " + path.getFileName() + " deleted by user _...");
            ByteBufSender.sendFileOpt(channel, buf, (byte) 31);
        } else {
            sendFileNotFound(channel, buf, path);
            System.out.println("file "+ path.getFileName() + " not found");//TODO
        }
        currentState = State.IDLE;
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
        } else {
            sendFileNotFound(channel, buf, path);
        }
        currentState = State.IDLE;
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
        } else {
            sendFileNotFound(channel, buf, path);
        }
        currentState = State.IDLE;
    }

    private void sendFileNotFound(Channel channel, ByteBuf buf, Path path) {
        System.out.println("file not found");
        ByteBufSender.sendFileOpt(channel, buf, (byte) 0);
        ByteBufSender.sendFileName(channel, buf, path);//file not found
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
    private void sendWalkTree(Channel channel, ByteBuf buf, ChannelFutureListener finishListener) throws IOException {
        ArrayList<Path> list = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get("."), new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    /*Integer.MAX_VALUE*/ 1, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            list.add(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        for (Path path : list) {
            out.writeUTF(path.toString());
        }
        byte[] bytes = baos.toByteArray();
        buf.writeBytes(bytes);
        ByteBufSender.sendFileOpt(channel, buf, (byte) 35);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }




    //System.out.println(bytes);
//        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//        DataInputStream in = new DataInputStream(bais);
//        ArrayList<Path> paths = new ArrayList<>();
//        while (in.available() > 0) {
//            String element = in.readUTF();
//            paths.add(Paths.get(element));
//        }
//        System.out.println(paths);

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


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}