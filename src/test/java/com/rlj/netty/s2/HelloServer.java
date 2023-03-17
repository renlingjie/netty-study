package com.rlj.netty.s2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-01
 */
public class HelloServer {
    public static void main(String[] args) {
        // 1、服务端准备建立一个工厂(服务端启动器)，实现服务端通信功能
        new ServerBootstrap()
                // 2、招募工人(Worker)和项目经理(Boss)
                .group(new NioEventLoopGroup())
                // 3、指定和客户(端)物资(数据)交流的通道(即和客户端IO交互的Channel)
                .channel(NioServerSocketChannel.class)
                // 4、指定工人(Worker)要做的事情，但是暂时不开工，直至客户那边有订单过来
                // 为什么不指定项目经理(Boss)要做的事情？因为他们只负责接单(accept事件)
                .childHandler(
                    // 5、工人(Worker)具体要做的事情流水线清单(nioSocketChannel.pipeline())，
                    // 按照清单里面顺序添加(addLast)的工序(Handler)，形成流水线
                    new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 5.1、工序1(Handler1)：将ByteBuf数据类型转为字符串
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        // 5.2、工序2(Handler2)：自定义Handler，触发读事件以后，打印Handler1转换的字符串的内容
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                }).bind(8080); // 6、工厂联系热线
    }
}
