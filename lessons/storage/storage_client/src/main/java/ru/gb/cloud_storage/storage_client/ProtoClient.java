package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.gb.cloud_storage.storage_common.ByteBufSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ProtoClient {
    public static void main(String[] args) throws Exception {

        sendFile("demo.txt", future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
            }
        });

        requestFile("1demo.txt");
//        Network.getInstance().stop();




//        ByteBuf buf = null;
//        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
//        buf.writeByte((byte) 40);
//        Network.getInstance().getCurrentChannel().write(buf);
//
//        byte[] filenameBytes = Paths.get("1","1.txt").getFileName().toString().getBytes(StandardCharsets.UTF_8);
//        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
//        buf.writeInt(filenameBytes.length);
//        Network.getInstance().getCurrentChannel().write(buf);
//        receiveFile(Paths.get("1","1.txt"),Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
//            }
//        });


//        Thread.sleep(2000);
//        FileSender.sendFile(Paths.get("demo1.txt"), Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
//            }
//        });
    }

    private static void sendFile(String fileName, ChannelFutureListener finishListener) throws IOException, InterruptedException {
        Path path = Paths.get(fileName);

        Channel channel = initChannel();
//        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBuf buf = null;
        ByteBufSender.sendFileOpt(channel, buf, (byte)20);
        ByteBufSender.sendFileName(channel, buf, path);
        ByteBufSender.sendFile(channel, buf, path, finishListener);


//        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
//        if (finishListener != null) {
//            transferOperationFuture.addListener(finishListener);
//        }
    }

    private static Channel initChannel() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();

        return Network.getInstance().getCurrentChannel();
    }

    private static void requestFile(String fileName) throws InterruptedException {

        Path path = Paths.get(fileName);
        ByteBuf buf = null;

        Channel channel = initChannel();
        ByteBufSender.sendFileOpt(channel, buf, (byte)40);
        ByteBufSender.sendFileName(channel, buf, path);

//        State currentState = State.FILE_LENGTH;
//        long fileLength = ByteBufReceiver.receiveFileLength(buf, currentState);
//        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("_from_Server_" + fileName));
//        System.out.println("STATE: Start file receiving");
//
//        ByteBufReceiver.receiveFile(buf, out, fileLength);



        //long fileLength = ByteBufReceiver.receiveFileLength(buf, currentState);
//        while (buf.readableBytes() > 0) {
//            out.write(buf.readByte());
//            receivedFileLength++;
//            if (fileLength == receivedFileLength) {
//                //currentState = State.IDLE;
//                System.out.println("File received");
//                out.close();
//                break;
//                /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
//            }
//        }
    }
}
