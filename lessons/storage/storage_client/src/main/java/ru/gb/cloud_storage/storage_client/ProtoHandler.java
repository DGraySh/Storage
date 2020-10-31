package ru.gb.cloud_storage.storage_client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.gb.cloud_storage.storage_common.ByteBufReceiver;
import ru.gb.cloud_storage.storage_common.State;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private Path userDir = Path.of("./user_directory");


    public ProtoHandler() throws IOException {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readBytes = buf.readByte();
                switch (readBytes) {
                    case (byte) 45: receiveFile(buf);
                        break;
                    case (byte) 31: System.out.println("file deleted");
                        break;
                    case (byte) 32: System.out.println("file renamed");
                        break;
                    case (byte) 33: System.out.println("file moved");
                        break;
                    case (byte) 35: receiveFileTree(buf);
                        break;
                    case (byte) 0: receiveFileNotFound(buf);
                        break;
                    default: System.out.println("ERROR: Invalid first byte - " + readBytes);
                    break;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void receiveFile(ByteBuf buf) {
        Path path = Path.of(ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH));
        long fileLength = ByteBufReceiver.receiveFileLength(buf, State.FILE_LENGTH);
        String fileName = userDir.resolve(path.getFileName()).toString();
        if (Files.notExists(Path.of(fileName))) {
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName))) {
                System.out.println("STATE: Start file receiving");
                ByteBufReceiver.receiveFile(buf, out, fileLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else System.out.println("File already exists");//TODO request for overwrite file in GUI
        currentState = State.ERROR;
//        buf.release();
    }

    private List<Path> receiveFileTree(ByteBuf buf) throws IOException {
        System.out.println("STATE: Start tree bytes receiving");
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
        System.out.println("Tree received");
        return paths;
    }

    private void receiveFileNotFound(ByteBuf buf) {
        String fileName = ByteBufReceiver.receiveFileName(buf, State.NAME_LENGTH);
        System.out.printf("file %s not found", fileName);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
