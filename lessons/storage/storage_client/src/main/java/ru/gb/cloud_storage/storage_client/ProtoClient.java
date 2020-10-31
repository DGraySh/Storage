package ru.gb.cloud_storage.storage_client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.ByteBufSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ProtoClient {
    private static final Logger logger = LogManager.getLogger(ProtoClient.class);

    public static void main(String[] args) throws Exception {

        Channel channel = initChannel();

        sendFile("./1/2/2.txt", channel, ProtoClient::operationComplete);
//        requestFile(channel, Path.of("./1/3.txt"));
//        Thread.sleep(2000);
//        deleteFile(channel, Path.of("./1/1.txt"));
//        Thread.sleep(2000);
//        moveFile(channel, Path.of("./1/3.txt"), Path.of("./1/5/31.txt"));
//        Thread.sleep(4000);
//        renameFile(channel, Path.of("./1/7.txt"), Path.of("./1/71.txt"));
//        Thread.sleep(2000);
//        requestFileTree(channel);
//        Thread.sleep(2000);

        Network.getInstance().stop();


    }

    private static Channel initChannel() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    private static void sendFile(String fileName, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            ByteBufSender.sendFileOpt(channel, (byte) 20);
            ByteBufSender.sendFileName(channel, path);
            ByteBufSender.sendFile(channel, path, finishListener);
        } else
            logger.error("File {} doesn't exist", path.getFileName());
    }

    private static void requestFile(Channel channel, Path path) {
        ByteBufSender.sendFileOpt(channel, (byte) 40);
        ByteBufSender.sendFileName(channel, path);
    }

    private static void deleteFile(Channel channel, Path path) {
        ByteBufSender.sendFileOpt(channel, (byte) 31);
        ByteBufSender.sendFileName(channel, path);
    }

    private static void moveFile(Channel channel, Path oldPath, Path newPath) {
        ByteBufSender.sendFileOpt(channel, (byte) 33);
        ByteBufSender.sendFileName(channel, oldPath);
        ByteBufSender.sendFileName(channel, newPath);
    }

    private static void renameFile(Channel channel, Path oldPath, Path newPath) {
        moveFile(channel, oldPath, newPath);
    }

    private static void requestFileTree(Channel channel) {
        ByteBufSender.sendFileOpt(channel, (byte) 35);
    }

    private static void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            logger.error("File sending error");
        }
        if (future.isSuccess()) {
            logger.info("File successfully sent");
        }
    }
}