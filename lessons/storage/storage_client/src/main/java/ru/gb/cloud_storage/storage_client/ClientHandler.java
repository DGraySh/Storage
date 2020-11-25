package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.CallBackReceive;
import ru.gb.cloud_storage.storage_common.CallMeBack;
import ru.gb.cloud_storage.storage_common.State;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final Path userDIr = Paths.get("./user_directory");
    private State currentState = State.IDLE;

    private CallMeBack cb = null;
    private CallBackReceive cbd = null;
    private Channel channel = null;
    private ChannelHandlerContext ctx = null;

    public ClientHandler(CallMeBack cb) {
        this.cb = cb;
        logger.info("protohandler init");
    }
//    public ProtoHandler(/*Channel channel, */CallMeBack cb, CallBackReceive cbr) {
////        this.channel = channel;
//        this.cb = cb;
//        this.cbd = cbr;
//        logger.info("protohandler init");
//    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.ctx = ctx;

        byte[] arr = new byte[((ByteBuf) msg).readableBytes()];
        ((ByteBuf) msg).readBytes(arr);
        switch (arr[0]) {
/*
                    case (byte) 45:
                        receiveFile(buf);
                        break;
*/
            case (byte) 31:
                logger.info("File deleted");
                break;
            case (byte) 32:
                logger.info("File renamed");
                break;
            case (byte) 33:
                logger.info("File moved");
                break;
/*
                    case (byte) 35:
                        logger.info(receiveFileTree(buf, cb));
                        break;
*/
            case (byte) 50:
                receiveFileList(arr, cb);
                break;
/*
                    case (byte) 10:
                        fileAlreadyExist(buf);
                        break;
                    case (byte) 11:
                        receiveFileNotFound(buf);
                        break;
*/
            case (byte) 20:
                logger.info("filename received successfully");
                break;
            case (byte) 22:
                logger.error("filename receiving error");
                break;
            default:
                logger.error("ERROR: Invalid first byte -");
                break;
        }
    }

    private void receiveFileList(byte[] arr, CallMeBack cb) throws IOException {

        byte[] bytes = Arrays.copyOfRange(arr, 1, arr.length);//TODO length of array to check sequence
        System.out.println(Arrays.toString(bytes));

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        ArrayList<String> list = new ArrayList<>();

        while (in.available() > 0) {
            String element = in.readUTF();
            list.add(element);
        }

        logger.info("Dirs received");
        System.out.println("handler:" + list);

        Platform.runLater(() -> cb.callMe(list));
        bais.close();
        in.close();
    }

/*
    private void receiveFile(ByteBuf buf) throws IOException {
        Path path = Paths.get(ByteBufReceiver.receiveFileName(ctx ,buf, State.NAME_LENGTH));
        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
        Files.createDirectories(userDIr);
        String fileName = userDIr.resolve(path.getFileName()).toString();
        if (Files.notExists(Paths.get(fileName))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
                logger.info("Start receiving file {}", path.getFileName());
                ByteBufReceiver.receiveFile(buf, out, fileLength, logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            logger.warn("File {} already exist, overwrite it?", path.getFileName());
        currentState = State.ERROR;
        //TODO request for overwrite file in GUI
    }

    private List<Path> receiveFileTree(ByteBuf buf, CallMeBack cb) throws IOException {

        byte[] bytes = new byte[buf.readableBytes()];

        while (buf.readableBytes() > 0) {
            buf.readBytes(bytes);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        ArrayList<Path> paths1 = new ArrayList<>();

        while (in.available() > 0) {
            String element = in.readUTF();
            paths1.add(Paths.get(element));
        }
        logger.info("File tree received");
//        System.out.println(paths1.stream().map(Path::toString).collect(Collectors.toList()));

//        UI.setPaths(paths);
        return paths1;
    }

    private void receiveFileNotFound(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(ctx, buf, State.NAME_LENGTH);
        logger.warn("file {} not found", fileName);
    }

    private void fileAlreadyExist(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(ctx, buf, State.NAME_LENGTH);
        logger.warn("file {} already exist", fileName);
    }
*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
