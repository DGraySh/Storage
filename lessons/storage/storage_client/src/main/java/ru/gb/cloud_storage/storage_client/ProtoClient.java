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

        sendFile("demo.txt", ProtoClient::operationComplete);
        requestFile(Path.of("./1/3.txt"));
        Thread.sleep(1000);
        Network.getInstance().stop();
        deleteFile(Path.of("./1/1.txt"));
        Thread.sleep(1000);
        Network.getInstance().stop();
        moveFile(Path.of("./1/3.txt"), Path.of("./1/5/31.txt"));
        Thread.sleep(1000);
        Network.getInstance().stop();
        renameFile(Path.of("./1/7.txt"), Path.of("./1/71.txt"));
        Thread.sleep(1000);
        Network.getInstance().stop();
        requestFileTree();

    }

    private static Channel initChannel() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    private static void sendFile(String fileName, ChannelFutureListener finishListener) throws IOException, InterruptedException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            Channel channel = initChannel();
            ByteBufSender.sendFileOpt(channel, (byte) 20);
            ByteBufSender.sendFileName(channel, path);
            ByteBufSender.sendFile(channel, path, finishListener);
        }
        else
            logger.error("File doesn't exist");
    }

    private static void requestFile(Path path) throws InterruptedException {
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, (byte) 40);
        ByteBufSender.sendFileName(channel, path);
    }

    private static void deleteFile(Path path) throws InterruptedException {
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, (byte) 31);
        ByteBufSender.sendFileName(channel, path);
    }

    private static void moveFile(Path oldPath, Path newPath) throws InterruptedException {
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, (byte) 33);
        ByteBufSender.sendFileName(channel, oldPath);
        ByteBufSender.sendFileName(channel, newPath);
    }

    private static void renameFile(Path oldPath, Path newPath) throws InterruptedException {
        moveFile(oldPath, newPath);
    }

    private static void requestFileTree() throws InterruptedException {
        Channel channel = initChannel();
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