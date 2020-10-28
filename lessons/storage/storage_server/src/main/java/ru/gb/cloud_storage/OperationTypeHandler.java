package ru.gb.cloud_storage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.gb.cloud_storage.storage_common.FileSender;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class OperationTypeHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, DATA, OPTS, OP_TYPE,
    }

    private State currentState = State.IDLE;
    private int nameLength;
    private long fileLength;
    private BufferedOutputStream out;
    ChannelHandlerContext ctx;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ctx = this.ctx;
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                if (buf.readByte() == (byte) 20) ReceiveFile(buf);
                else if
                (buf.readByte() == (byte) 30) FileOpts(buf);
                else if
                (buf.readByte() == (byte) 40) SendFile(buf, ctx.channel());
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    private void ReceiveFile(ByteBuf buf) throws IOException {
        currentState = State.NAME_LENGTH;
        long receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
        getFileName(buf);
        currentState = State.FILE_LENGTH;
        getFileLength(buf);
        currentState = State.DATA;
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (getFileLength(buf) == receivedFileLength) {
                currentState = State.IDLE;
                System.out.println("File received");
                out.close();
                break;
                /*Написать здесь отправку обратно md5-суммы файла для проверки на повреждения *///TODO
            }
        }
    }

    private void SendFile(ByteBuf buf, Channel channel) throws IOException {
        currentState = State.NAME_LENGTH;
        System.out.println("STATE: File sending");
//        getFileName(buf);
//        getFileLength(buf);
        FileSender.sendFile(Path.of(getFileName(buf)), ctx.channel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
            }
        });
    }

    private long getFileLength(ByteBuf buf) {
        if ((currentState == State.FILE_LENGTH) && (buf.readableBytes() >= 8)) {
            System.out.println("STATE: File length received - " + fileLength);
            fileLength = buf.readLong();
            //currentState = State.DATA;
        }
        if (fileLength != 0)
            return fileLength;
        else
            throw new NullPointerException("File name is missing");
    }

    private String getFileName(ByteBuf buf) throws FileNotFoundException {
        byte[] fileName = null;
        if ((currentState == State.NAME_LENGTH) && (buf.readableBytes() >= 4)) {
            System.out.println("STATE: Get filename length");
            nameLength = buf.readInt();
            currentState = State.NAME;
        }

        if ((currentState == State.NAME) && (buf.readableBytes() >= nameLength)) {
            fileName = new byte[nameLength];
            buf.readBytes(fileName);
            System.out.println("STATE: Filename received - _" + new String(fileName, StandardCharsets.UTF_8));
            out = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
            //currentState = State.FILE_LENGTH;
        }
        if (fileName != null) {
            return new String(fileName);
        } else
            throw new NullPointerException("File name is missing");
    }

    private void FileOpts(ByteBuf buf) throws IOException {
        currentState = State.OPTS;
        System.out.println("STATE: Start file operations");
        while (buf.readableBytes() > 0) {
            if (currentState == State.OPTS) {
                if (buf.readByte() == (byte) 31) DeleteFile(buf);
                if (buf.readByte() == (byte) 32) RenameFile(buf);
                if (buf.readByte() == (byte) 33) MoveFile(buf);
            }
        }
    }

    private void DeleteFile(ByteBuf buf) throws IOException {
    }//TODO

    private void RenameFile(ByteBuf buf) throws IOException {
    }//TODO

    private void MoveFile(ByteBuf buf) throws IOException {
    }//TODO

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}