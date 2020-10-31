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

    private Path userDir = Path.of("user_dir");
    private String username;
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

/*
    private void sendFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        sendRequestedFile(buf, ctx.channel(), OperationTypeHandler::operationComplete);
    }
*/

    private void FileOpts(Channel channel, ByteBuf buf, byte read) throws IOException {
        currentState = State.OPTS;
        System.out.println("STATE: Start file operations");
        if (read == (byte) 31) deleteFile(channel, buf);
        if ((read == (byte) 33) || (read == (byte) 32)) moveFile(channel, buf);
        if (read == (byte) 35) sendWalkTree(channel, buf, OperationTypeHandler::operationComplete);

    }

    private void sendRequestedFile(ByteBuf buf, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        if (Files.exists(path)) {
            ByteBufSender.sendFileOpt(channel, buf, (byte) 45);
            ByteBufSender.sendFileName(channel, buf, path);
            System.out.println("STATE: Start requested file sending");
            ByteBufSender.sendFile(channel, buf, path, finishListener);
        } else {
            sendFileNotFound(channel, buf, path);
            System.out.printf("file %s not found", path.getFileName());
        }
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        long receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
        try (BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream("from_client_" +
                        ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH)))) {
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
        Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
            System.out.printf("file %s deleted by user %s%n", path.getFileName(), username);
            ByteBufSender.sendFileOpt(channel, buf, (byte) 31);
        } else {
            sendFileNotFound(channel, buf, path);
            System.out.printf("file %s not found%n", path.getFileName());
        }
        currentState = State.IDLE;
    }

/*
    private void renameFile(Channel channel, ByteBuf buf) throws IOException {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        Path path = Paths.get(fileName);
        String newFileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        if (Files.exists(path)) {
            Files.move(path, path.resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
            ByteBufSender.sendFileOpt(channel, buf, (byte) 32);
            System.out.println("file " + fileName + " renamed to " + newFileName + " by user _...");
        } else {
            sendFileNotFound(channel, buf, path);
        }
        currentState = State.IDLE;
    }
*/

    private void moveFile(Channel channel, ByteBuf buf) throws IOException {
        Path oldPath = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        Path newPath = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        if (Files.exists(oldPath)) {
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            if (oldPath.getParent().compareTo(newPath.getParent()) !=0) {
                ByteBufSender.sendFileOpt(channel, buf, (byte) 32);
                System.out.printf("file %s renamed to %s by user %s%n", oldPath.getFileName(), newPath.getFileName(), username);
            }
            else {
                ByteBufSender.sendFileOpt(channel, buf, (byte) 33);
                System.out.printf("file %s moved to %s by user %s%n", oldPath.getFileName(), newPath, username);
            }
        } else {
            sendFileNotFound(channel, buf, oldPath);
        }
        currentState = State.IDLE;
    }

    private void sendWalkTree(Channel channel, ByteBuf buf, ChannelFutureListener finishListener) throws IOException {
        ArrayList<Path> list = new ArrayList<>();
        try {
            Files.walkFileTree(userDir, new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    /*Integer.MAX_VALUE*/ 3, new SimpleFileVisitor<>() {
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
            e.printStackTrace();
        }
        //convert filetree to byte[] and send
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

    private void sendFileNotFound(Channel channel, ByteBuf buf, Path path) throws IOException {
        System.out.println("file not found");
        ByteBufSender.sendFileOpt(channel, buf, (byte) 0);
        ByteBufSender.sendFileName(channel, buf, path);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}