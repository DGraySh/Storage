/*
package ru.gb.cloud_storage.storage_client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.CallBackReceive;
import ru.gb.cloud_storage.storage_common.CallMeBack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ProtoClient {
    private static final Logger logger = LogManager.getLogger(ProtoClient.class);
    private static CallMeBack cb;

    public ProtoClient(CallMeBack cb) {
        this.cb = cb;
    }

    public static void main(String[] args) throws Exception {


//        Channel channel = initChannel(cb);
//        requestFileTree(channel);
//        sendFile("./1/2/2.txt", channel, ProtoClient::operationComplete);
//        requestFile(channel, Path.of("./1/3.txt"));
//        Thread.sleep(2000);
//        deleteFile(channel, Path.of("./1/1.txt"));
//        Thread.sleep(1000);
//        requestFileTree(channel);
//        Thread.sleep(1000);
//
//        moveFile(channel, Paths.get("./1/5/3.txt"), Paths.get("./1/5/31.txt"));
//        Thread.sleep(4000);
//        renameFile(channel, Path.of("./1/7.txt"), Path.of("./1/71.txt"));
//        Thread.sleep(2000);
//
//        Thread.sleep(2000);

//        Network.getInstance().stop();


    }

    public static Channel initChannel(CallMeBack cb, CallBackReceive cbr) throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter, cb)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    private static void sendFile(String fileName, Channel channel, ChannelFutureListener finishListener) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            ByteBufSender.sendFile(channel, path, finishListener);
        } else
            logger.error("File {} doesn't exist", path.getFileName());
    }

    private static void requestFile(Channel channel, Path path) {
    }

    private static void deleteFile(Channel channel, Path path) {
    }

    private static void moveFile(Channel channel, Path oldPath, Path newPath) {
        ByteBufSender.sendFileOpt(channel, (byte) 33);
        ByteBufSender.sendFileName(channel, oldPath);
        ByteBufSender.sendFileName(channel, newPath);
    }

    private static void renameFile(Channel channel, Path oldPath, Path newPath) {
        moveFile(channel, oldPath, newPath);
    }

    public static void requestFileTree(Channel channel) {
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
}*/
