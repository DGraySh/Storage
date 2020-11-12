package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class OperationTypeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(OperationTypeHandler.class);
    private String username;
    private final String userRootDir = "user_dir_on_server";         //TODO take it from auth

    private State currentState = State.IDLE;

    private static void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
        }
        if (future.isSuccess()) {
            logger.info("File successfully sent");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readBytes = buf.readByte();
                if (readBytes == (byte) 20) {
                    receiveFile(ctx.channel(), buf);
                    break;
                }
                if ((readBytes > (byte) 30) && (readBytes < (byte) 40)) {
                    fileOperations(ctx.channel(), buf, readBytes);
                    break;
                }
                if (readBytes == (byte) 40) {
                    sendRequestedFile(ctx.channel(), buf, OperationTypeHandler::operationComplete);
                    break;
                }
                if (readBytes == (byte) 50) {
                    sendDirs(ctx.channel(), buf, OperationTypeHandler::operationComplete);
                    break;
                }
            }
        }
    }

/*
    private void sendFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        sendRequestedFile(buf, ctx.channel(), OperationTypeHandler::operationComplete);
    }
*/

    private void fileOperations(Channel channel, ByteBuf buf, byte read) throws IOException {
        if (read == (byte) 31) deleteFile(channel, buf);
        if ((read == (byte) 33) || (read == (byte) 32)) moveFile(channel, buf);
        if (read == (byte) 35) sendWalkTree(channel, buf, OperationTypeHandler::operationComplete);

    }

    private void sendRequestedFile(Channel channel, ByteBuf buf, ChannelFutureListener finishListener) throws IOException {
        Path path = Paths.get(ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH));
        if (Files.exists(path)) {
            ByteBufSender.sendFileOpt(channel, (byte) 45);
            ByteBufSender.sendFileName(channel, path);
            logger.info("Start requested file {} sending", path.getFileName());
            ByteBufSender.sendFile(channel, path, finishListener);
        } else {
            sendFileNotFound(channel, path);
            logger.info("file {} not found", path.getFileName());
        }
    }

    private void receiveFile(Channel channel, ByteBuf buf) throws IOException {
        Path path = Paths.get(ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH));
        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
        Files.createDirectories(Paths.get(userRootDir));
        String fileName = Paths.get(userRootDir).resolve(path.getFileName()).toString();
        if (Files.notExists(Paths.get(fileName))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
                logger.info("Start receiving file {}", path.getFileName());
                ByteBufReceiver.receiveFile(buf, out, fileLength, logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            sendFileAlreadyExist(channel, path);
        currentState = State.ERROR;
        //TODO request for overwrite file in GUI
    }

    private void deleteFile(Channel channel, ByteBuf buf) throws IOException {
        Path path = Paths.get(ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH));
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
            logger.info("file {} deleted by user {}", path.getFileName(), username);
            ByteBufSender.sendFileOpt(channel, (byte) 31);
        } else {
            sendFileNotFound(channel, path);
            logger.warn("file {} not found", path.getFileName());
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
        Path oldPath = Paths.get(ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH));
        Path newPath = Paths.get(ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH));
        if (Files.exists(oldPath)) {
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            if (oldPath.getParent().compareTo(newPath.getParent()) != 0) {
                ByteBufSender.sendFileOpt(channel, (byte) 32);
                logger.info("file {} renamed to {} by user {}", oldPath.getFileName(), newPath.getFileName(), username);
            } else {
                ByteBufSender.sendFileOpt(channel, (byte) 33);
                logger.info("file {} moved to {} by user {}", oldPath.getFileName(), newPath, username);
            }
        } else {
            sendFileNotFound(channel, oldPath);
        }
        currentState = State.IDLE;
    }

    private void sendDirs(Channel channel, ByteBuf buf, ChannelFutureListener finishListener) throws IOException {

        String requestedDir = ByteBufReceiver.receiveFileName(channel, buf, State.NAME_LENGTH);
/*        if (requestedDir != null)
            ByteBufSender.sendFileOpt(channel, (byte) 20);
        else
            ByteBufSender.sendFileOpt(channel, (byte) 22);*/

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        for (String s : new FileBrowser(userRootDir).getFileList(requestedDir)) {
            out.writeUTF(s);
        }

        buf.writeBytes(baos.toByteArray());
        ByteBufSender.sendFileOpt(channel, (byte) 50);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private void sendWalkTree(Channel channel, ByteBuf buf, ChannelFutureListener finishListener) throws IOException {
        ArrayList<Path> list = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(userRootDir), new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    /*Integer.MAX_VALUE*/ 1, new SimpleFileVisitor<Path>() {
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
            logger.error("Exception in WalkTree");
        }
        //convert file tree to byte[] and send
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        for (Path path : list) {
            out.writeUTF(path.toString());
        }
        byte[] bytes = baos.toByteArray();
        buf.writeBytes(bytes);
        ByteBufSender.sendFileOpt(channel, (byte) 35);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private void sendFileNotFound(Channel channel, Path path) {
        logger.warn("File {} not found", path.getFileName());
        ByteBufSender.sendFileOpt(channel, (byte) 11);
        ByteBufSender.sendFileName(channel, path);
    }

    private void sendFileAlreadyExist(Channel channel, Path path) {
        logger.warn("File {} already exist on server, overwrite it?", path.getFileName());
        ByteBufSender.sendFileOpt(channel, (byte) 10);
        ByteBufSender.sendFileName(channel, path);
    }


    private void sendFile(String fileName, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Paths.get(fileName);
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBufSender.sendFileOpt(channel, (byte) 20);
        ByteBufSender.sendFileName(channel, path);
        ByteBufSender.sendFile(channel, path, finishListener);

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