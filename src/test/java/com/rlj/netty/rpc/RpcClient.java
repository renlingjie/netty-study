package com.rlj.netty.rpc;

import com.rlj.message.RpcRequestMessage;
import com.rlj.protocol.MessageCodec;
import com.rlj.protocol.ProtocolFrameDecoder;
import com.rlj.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-28
 */
@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        // RPC响应消息处理器
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new ProtocolFrameDecoder());
                    channel.pipeline().addLast(LOGGING_HANDLER);
                    // 因为我们的MessageCodec无法加@Sharable注解，因此不能共享，每次建立连接都需要单独new
                    channel.pipeline().addLast(new MessageCodec());
                    channel.pipeline().addLast(RPC_HANDLER);
                }
            });
            // 建立起连接后，就发送RPC请求消息
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            // RPC请求消息传入6个参数：消息序列号、请求方法所在接口的全限定类名、方法名、方法返回值类型、方法参数类型数组
            // (因为我们参数只有一个String类型的名字，因此数组中就只有一个String.class)、方法参数数组
            channel.writeAndFlush(new RpcRequestMessage(
                    1,"com.rlj.server.service.RpcTestService","sayHello",
                    String.class,new Class[]{String.class},new Object[]{"张三"}
            ));
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Client error",e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
