package com.rlj.netty.s1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
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
        // 1、客户端启动器。将下面链式的Netty组件组装到一起，作为整体的服务进行启动
        new Bootstrap()
                // 2、创建NioEventLoopGroup，和服务端目的一样
                .group(new NioEventLoopGroup())
                // 3、指定Channel通道类型，有NioSocketChannel、OioSocketChannel(其实就是BIO)等等，这里就用NIO的
                .channel(NioSocketChannel.class)
                // 4、添加SocketChannel的处理器——ChannelInitializer处理器(仅执行一次)，它的作用是待客户端SocketChannel
                // 建立连接后，执行initChannel以便添加更多的处理器
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        // 这里即添加具体的Handler，添加一个将写入的字符串转换成ByteBuf的Handler处理器
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5、指定要连接的服务器和端口
                .connect("127.0.0.1",8080)
                // 6、Netty中很多方法都是异步的，比如这里的connect，此时需要使用sync()方法等待connect建立连接完毕
                .sync()
                // 7、获取Channel对象，它是通道抽象，可以进行数据读写操作
                .channel()
                // 8、写入消息并清空缓冲区，这里会用到上面SocketChannel的处理器，将String => ByteBuf
                .writeAndFlush(new Date() + ":Hello World");
    }
}
