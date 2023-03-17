package com.rlj.server;

import com.rlj.protocol.MessageCodec;
import com.rlj.protocol.ProtocolFrameDecoder;
import com.rlj.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 将不会记录多个EventLoop某些数据的线程安全的Handler抽取出来，以便多个服务端复用
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        LoginRequestMessageHandler LOGIN_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_HANDLER = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler GROUP_CREATE_HANDLER = new GroupCreateRequestMessageHandler();
        GroupChatRequestMessageHandler GROUP_CHAT_HANDLER = new GroupChatRequestMessageHandler();
        GroupJoinRequestMessageHandler GROUP_JOIN_HANDLER = new GroupJoinRequestMessageHandler();
        GroupMembersRequestMessageHandler GROUP_MEMBERS_HANDLER = new GroupMembersRequestMessageHandler();
        GroupQuitRequestMessageHandler GROUP_QUIT_HANDLER = new GroupQuitRequestMessageHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss,worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 1、Handler1：预设长度解决Tcp黏包半包，参数与我们的协议内容有关
                    socketChannel.pipeline().addLast(new ProtocolFrameDecoder());
                    // 2、Handler2：打印日日志级别为Debug
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    // 3、Handler3：使用自定义的编解码器(不能被共享)
                    socketChannel.pipeline().addLast(new MessageCodec());
                    // 4、Handler4：Netty提供了一个，检查客户端与服务端多久没有发生读或写事件的IdleStateHandler。在服务端，一般设置第
                    // 一个参数readerIdleTimeSeconds，为10表示如果10s都没有读到客户端发来的数据，就触发IdleState#READER_IDLE事件
                    socketChannel.pipeline().addLast(new IdleStateHandler(10,0,0));
                    // 上面的IdleState#READER_IDLE事件触发后，应该被一个Handler捕捉并处理，因为IdleStateHandler涉及读写，对好来一个双向
                    // (入站、出站都能用)的Handler--ChannelDuplexHandler
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        // IdleState#READER_IDLE事件属于自定义事件，自定义事件都在userEventTriggered中被捕捉、触发
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 如果触发的是读空闲事件，就把对应的客户端连接断开
                            if (event.state() == IdleState.READER_IDLE) {
                                log.debug("已经 10s 没有读到数据了，客户端5s一次的心跳包没来过，肯定挂了");
                                ctx.channel().close();
                            }
                        }
                    });
                    // 5、Handler5：添加处理登录请求的Handler
                    socketChannel.pipeline().addLast(LOGIN_HANDLER);
                    // 6、Handler6：添加处理聊天请求的Handler
                    socketChannel.pipeline().addLast(CHAT_HANDLER);
                    // 7、Handler7：添加处理创建聊天室请求的Handler
                    socketChannel.pipeline().addLast(GROUP_CREATE_HANDLER);
                    // 8、Handler8：添加处理聊天室聊天请求的Handler
                    socketChannel.pipeline().addLast(GROUP_CHAT_HANDLER);
                    // 9、Handler9：添加处理加入聊天室请求的Handler
                    socketChannel.pipeline().addLast(GROUP_JOIN_HANDLER);
                    // 10、Handler10：添加处理查询聊天室成员请求的Handler
                    socketChannel.pipeline().addLast(GROUP_MEMBERS_HANDLER);
                    // 11、Handler11：添加处理解散聊天室请求的Handler
                    socketChannel.pipeline().addLast(GROUP_QUIT_HANDLER);
                    // 12、Handler12：添加处理客户端正常、异常断开连接的Handler
                    socketChannel.pipeline().addLast(QUIT_HANDLER);

                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Server error",e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
