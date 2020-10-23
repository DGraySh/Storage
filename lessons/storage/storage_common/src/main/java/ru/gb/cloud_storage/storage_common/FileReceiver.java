//package ru.gb.cloud_storage.storage_common;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufAllocator;
//import io.netty.channel.*;
//
//import java.io.BufferedOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class FileReceiver {
//    private BufferedOutputStream out;
//
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ByteBuf buf = ((ByteBuf) msg);
//            while (buf.readableBytes() > 0) {
//                if (currentState == State.IDLE) {
//                    byte readed = buf.readByte();
//                    if (readed == (byte) 20) {
//                        currentState = State.NAME_LENGTH;
//                        receivedFileLength = 0L;
//                        System.out.println("STATE: Start file receiving");
//                    } else {
//                        System.out.println("ERROR: Invalid first byte - " + readed);
//                    }
//                }
//
//
//                if (currentState == State.NAME_LENGTH) {
//                    if (buf.readableBytes() >= 4) {
//                        System.out.println("STATE: Get filename length");
//                        nextLength = buf.readInt();
//                        currentState = State.NAME;
//                    }
//                }
//
//                if (currentState == State.NAME) {
//                    if (buf.readableBytes() >= nextLength) {
//                        byte[] fileName = new byte[nextLength];
//                        buf.readBytes(fileName);
//                        System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
//                        out = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
//                        currentState = State.FILE_LENGTH;
//                    }
//                }
//
//                if (currentState == State.FILE_LENGTH) {
//                    if (buf.readableBytes() >= 8) {
//                        fileLength = buf.readLong();
//                        System.out.println("STATE: File length received - " + fileLength);
//                        currentState = State.DATA;
//                    }
//                }
//
//                if (currentState == State.DATA) {
//                    while (buf.readableBytes() > 0) {
//                        out.write(buf.readByte());
//                        receivedFileLength++;
//                        if (fileLength == receivedFileLength) {
//                            currentState = State.IDLE;
//                            System.out.println("File received");
//                            out.close();
//                            break;
//                        }
//                    }
//                }
//            }
//            if (buf.readableBytes() == 0) {
//                buf.release();
//            }
//        }
//        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
//        if (finishListener != null) {
//            transferOperationFuture.addListener(finishListener);
//        }
//    }
//}
