package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(OperationTypeHandler.class);
    private final Path userDir = Path.of("user_dir_on_server");
    private String username;
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
                if (readBytes == (byte) 20) receiveFile(ctx.channel(), buf);
                if ((readBytes > (byte) 30) && (readBytes < (byte) 40)) fileOperations(ctx.channel(), buf, readBytes);
                if (readBytes == (byte) 40)
                    sendRequestedFile(buf, ctx.channel(), OperationTypeHandler::operationComplete);
            }
        }
    }

    private void fileOperations(Channel channel, ByteBuf buf, byte read) throws IOException {
        if (read == (byte) 31) deleteFile(channel, buf);
        if ((read == (byte) 33) || (read == (byte) 32)) moveFile(channel, buf);
        if (read == (byte) 35) sendWalkTree(channel, buf, OperationTypeHandler::operationComplete);

    }

    private void sendRequestedFile(ByteBuf buf, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
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
        Path path = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
        Files.createDirectories(userDir);
        String fileName = userDir.resolve(path.getFileName()).toString();
        if (Files.notExists(Path.of(fileName))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
                logger.info("Start receiving file {}", path.getFileName());
                ByteBufReceiver.receiveFile(buf, out, fileLength, logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            sendFileAlreadyExist(channel, path);
        currentState = State.ERROR;
    }

    private void deleteFile(Channel channel, ByteBuf buf) throws IOException {
        Path path = Paths.get(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
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

    private void moveFile(Channel channel, ByteBuf buf) throws IOException {
        Path oldPath = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        Path newPath = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
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
            logger.error("Exception in WalkTree");
        }
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
        ByteBufSender.sendFileOpt(channel, (byte) 0);
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
