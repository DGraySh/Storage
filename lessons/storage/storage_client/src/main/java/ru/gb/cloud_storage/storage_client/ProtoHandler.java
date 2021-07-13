package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.State;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(ProtoHandler.class);
    private final Path userDIr = Path.of("./user_directory");
    private State currentState = State.IDLE;

    public ProtoHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readBytes = buf.readByte();
                switch (readBytes) {
                    case (byte) 45:
                        receiveFile(buf);
                        break;
                    case (byte) 31:
                        logger.info("File deleted");
                        break;
                    case (byte) 32:
                        logger.info("File renamed");
                        break;
                    case (byte) 33:
                        logger.info("File moved");
                        break;
                    case (byte) 35:
                        receiveFileTree(buf);
                        break;
                    case (byte) 10:
                        fileAlreadyExist(buf);
                        break;
                    case (byte) 0:
                        receiveFileNotFound(buf);
                        break;
                    default:
                        logger.error("ERROR: Invalid first byte - {}", readBytes);
                        break;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void receiveFile(ByteBuf buf) throws IOException {
        Path path = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
        Files.createDirectories(userDIr);
        String fileName = userDIr.resolve(path.getFileName()).toString();
        if (Files.notExists(Path.of(fileName))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
                logger.info("Start receiving file {}", path.getFileName());
                ByteBufReceiver.receiveFile(buf, out, fileLength, logger);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            logger.warn("File {} already exist, overwrite it?", path.getFileName());
        currentState = State.ERROR;
    }

    private List<Path> receiveFileTree(ByteBuf buf) throws IOException {
        byte[] bytes = new byte[buf.readableBytes()];

        while (buf.readableBytes() > 0) {
            buf.readBytes(bytes);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        ArrayList<Path> paths = new ArrayList<>();

        while (in.available() > 0) {
            String element = in.readUTF();
            paths.add(Paths.get(element));
        }
        logger.info("File tree received");
        return paths;
    }

    private void receiveFileNotFound(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        logger.warn("file {} not found", fileName);
    }

    private void fileAlreadyExist(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        logger.warn("file {} already exist", fileName);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
