package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.gb.cloud_storage.storage_common.ByteBufSender;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ProtoClient {
    public static void main(String[] args) throws Exception {

//        sendFile("demo.txt", ProtoClient::operationComplete);
        requestFile(Path.of("./1/3.txt"));
        Thread.sleep(1000);
        Network.getInstance().stop();
//        deleteFile(Path.of("./1/1.txt"));
//        Thread.sleep(1000);
//        Network.getInstance().stop();
//        moveFile(Path.of("./1/3.txt"), Path.of("./1/5/31.txt"));
//        Thread.sleep(1000);
//        Network.getInstance().stop();
//        renameFile(Path.of("./1/7.txt"), Path.of("./1/71.txt"));
//        Thread.sleep(1000);
//        Network.getInstance().stop();
//        requestFileTree();

    }

    private static Channel initChannel() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    private static void sendFile(String fileName, ChannelFutureListener finishListener) throws IOException, InterruptedException {
        ByteBuf buf = null;
        Path path = Paths.get(fileName);
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte)20);
        ByteBufSender.sendFileName(channel, buf, path);
        ByteBufSender.sendFile(channel, buf, path, finishListener);
    }

    private static void requestFile(Path path) throws InterruptedException, IOException {
        ByteBuf buf = null;
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte) 40);
        ByteBufSender.sendFileName(channel, buf, path);
    }

    private static void deleteFile(Path path) throws InterruptedException, IOException {
        ByteBuf buf = null;
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte) 31);
        ByteBufSender.sendFileName(channel, buf, path);
    }

    private static void moveFile(Path oldPath, Path newPath) throws InterruptedException, IOException {
        ByteBuf buf = null;
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte) 33);
        ByteBufSender.sendFileName(channel, buf, oldPath);
        ByteBufSender.sendFileName(channel, buf, newPath);
    }

    private static void renameFile(Path oldPath, Path newPath) throws InterruptedException, IOException {
        moveFile(oldPath, newPath);
    }

    private static void requestFileTree() throws InterruptedException {
        ByteBuf buf = null;
        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte) 35);
    }

    private static void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
        }
        if (future.isSuccess()) {
            System.out.println("Файл успешно передан");
        }
    }
}