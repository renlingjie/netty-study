package com.rlj.netty.s7;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-17
 */
public class HalfPackageServer {

    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            //设置服务器接收端缓冲区大小为10
            // serverBootstrap.option(ChannelOption.SO_RCVBUF,5);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LineBasedFrameDecoder(100));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            //此处打印输出，看看收到的内容是10次16字节，还是一次160字节
                            printBuf((ByteBuf) msg);
                            super.channelRead(ctx, msg);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            //阻塞等待连接
            channelFuture.sync();
            //阻塞等待释放连接
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("server error:" + e);
        } finally {
            // 释放EventLoopGroup
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static void printBuf(ByteBuf byteBuf){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i< byteBuf.writerIndex();i++) {
            stringBuilder.append(byteBuf.getByte(i));
            stringBuilder.append(" ");
        }

        stringBuilder.append("| 长度：");
        stringBuilder.append(byteBuf.writerIndex());
        stringBuilder.append("字节");
        System.out.println(stringBuilder);
    }
}
