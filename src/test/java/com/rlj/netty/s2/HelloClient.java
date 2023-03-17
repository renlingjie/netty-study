package com.rlj.netty.s2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Date;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-01
 */
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        // 7、来了一个客户(客户端启动器)，实现客户端通信功能
        new Bootstrap()
                // 8、招募项目经理(实际可以不用，因为客户端没有多个线程进行Worker/Boss，就当前线程
                // 用于连接服务端，所以这步可以省略，但是为了和服务端格式一致，可以加上)
                .group(new NioEventLoopGroup())
                // 9、指定和服务(端)物资(数据)交流的通道(即和服务端IO交互的Channel)
                .channel(NioSocketChannel.class)
                // 10、当前客户(若有第8步，当前客户就变身为项目经理)要做的事情，也是流水线，也暂时不开工
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 10.1、工序1(Handler1)：将字符串数据类型转为ByteBuf
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 11、拨打工厂联系热线，和工厂的项目经理洽谈(Boss的accept事件触发)
                .connect("127.0.0.1",8080)
                // 12、洽谈中(当前线程阻塞住，直至连接建立)
                .sync()
                // 13、洽谈结束后继续往下走，此时连接已经建立，就可以拿到连接通道了(双方贯通的物资通道)
                .channel()
                // 14、将物资发送给工厂加工
                .writeAndFlush(new Date() + ":Hello World");
    }
}
