package ru.gb.cloud_storage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ru.gb.cloud_storage.storage_common.ByteToMessageInboundHandler;
import ru.gb.cloud_storage.storage_common.MessageToByteOutboundHandler;

public class Server {
    public static void main(String[] args) throws Exception {
        new Server().run();
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MessageToByteOutboundHandler(), new ByteToMessageInboundHandler(), new OperationTypeHandler());
                        }
                    });
            // .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(8186).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}