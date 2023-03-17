package com.rlj.netty.s1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
        // 1、服务端启动器。将下面链式的Netty组件组装到一起，作为整体的服务进行启动
        new ServerBootstrap()
                // 2、通过调用group()方法，加入一个Worker+Boss组，组内的每个成员都是一个线程，都各自有一个Selector，
                // 只不过有的成员只负责处理accept事件(Boss)、有的成员只负责处理read/write事件(Worker)
                .group(new NioEventLoopGroup())
                // 3、指定Channel通道类型，有NioServerSocketChannel、OioServerSocketChannel(其实就是BIO)等等，这里就用NIO的
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                // 4、这里的Child特指上面group中的Worker，后面handler的作用就是将来启动时要告诉是Worker的成员需要做哪些事。也就是
                // 读写事件拿到数据后要怎么处理，是打印？还是保存到某个文件中？等等。之后只要往里面添加具体的Handler即可
                .childHandler(
                   // 4.1、Channel代表和客户端进行数据读写的通道，Initializer即初始化，负责添加具体的Handler
                    new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 这里即添加具体的Handler
                        nioSocketChannel.pipeline().addLast(new StringDecoder()); // Handler1：将ByteBuf数据类型转为字符串
                        // Handler2：自定义Handler，触发读事件以后，打印Handler1转换的字符串的内容
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                }).bind(8080); // 5、服务端的监听端口
    }
}
