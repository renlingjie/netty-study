package com.rlj.netty.s5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-01
 */
@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // ChannelFuture使用sync()同步阻塞，直至连接建立好后，拿到建立好的Channel
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("127.0.0.1", 8080)
                .sync()
                .channel();
        new Thread(() ->{
            Scanner scanner = new Scanner(System.in);
            while (true){
                String input = scanner.nextLine();
                if ("q".equals(input)){
                    channel.close();
                    // Channel关闭后的善后处理代码在这里写是不对的，因为channel.close()是通过
                    // 上面的NioEventLoopGroup执行的，而善后代码则是由这个线程执行的，我们无法
                    // 控制两个线程的执行顺序，很有可能善后代码反倒先执行
                    break;
                }
                channel.writeAndFlush(input);
            }
        },"input").start();
        ChannelFuture closeFuture = channel.closeFuture();
        // 同步方式 ---> 主线程执行到这里时阻塞住，直至上面新建的线程走完channel.close();才会继续往下运行后续的善后代码
        // closeFuture.sync();
        // log.debug("在这里写关闭后的善后代码");
        // 异步方式 ---> 主线程无阻塞运行到这里，当上面新建的线程走完channel.close()，才会执行这里的回调(善后代码)
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("在这里写关闭后的善后代码");
                group.shutdownGracefully(); // 执行完这里客户端才会真正关闭
            }
        });
    }
}
