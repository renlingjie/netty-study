package com.rlj.client;

import com.rlj.message.*;
import com.rlj.protocol.MessageCodec;
import com.rlj.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        // 用来进行线程同步协作，等待前置的所有线程完成倒计时后才会执行。 其中构造参数用来初始化等待计数值，await()用来等待计数归零后执行，
        // countDown()用来让计数减一。这样我下面另跑的线程System in在输入完用户名、密码发送登录消息后，就会在WAIT_FOR_LOGIN.await()
        // 处阻塞住，而当NioEventLoopGroup中的线程由channelRead事件拿到服务端登录响应减1后，就不再阻塞，才会继续向下运行，保证了正确的
        // 登录逻辑：客户端线程A发送登录请求阻塞住-->服务端线程B登录校验，返回结果-->客户端线程C由channelRead接收结果，countDown()减1
        // -->线程A得以进行后续的操作(其实也就是可以发送聊天信息给服务端了)
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        // 标识当前客户端登录状态，false：还未登录，true：已经登录
        AtomicBoolean IS_LOGIN = new AtomicBoolean(false);
        // 退出标识符
        AtomicBoolean EXIT = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 1、Handler1：预设长度解决Tcp黏包半包，参数与我们的协议内容有关
                    socketChannel.pipeline().addLast(new ProtocolFrameDecoder());
                    // 2、Handler2：打印日日志级别为Debug
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    // 3、Handler3：使用自定义的编解码器(不能被共享)
                    socketChannel.pipeline().addLast(new MessageCodec());
                    // 4、Handler4：Netty提供了一个，检查客户端与服务端多久没有发生读或写事件的IdleStateHandler。在客户端端，一般设置第
                    // 二个参数writerIdleTimeSeconds，为5表示如果5s都没有接收到服务端发来的数据(响应)，就触发IdleState#RWRITER_IDLE事件
                    socketChannel.pipeline().addLast(new IdleStateHandler(0,5,0));
                    // 上面的IdleState#READER_IDLE事件触发后，应该被一个Handler捕捉并处理，因为IdleStateHandler涉及读写，对好来一个双向
                    // (入站、出站都能用)的Handler--ChannelDuplexHandler
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        // IdleState#WRITER_IDLE事件属于自定义事件，自定义事件都在userEventTriggered中被捕捉、触发
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 如果触发的是读空闲事件，就把对应的客户端连接断开
                            if (event.state() == IdleState.WRITER_IDLE) {
                                log.debug("已经 5s 没有写入数据了，发送心跳包证明我还活着");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    // 5、Handler5：客户端发送登录请求、接收登录响应
                    socketChannel.pipeline().addLast("Client login handler",new ChannelInboundHandlerAdapter(){
                        // 当连接建立好后(channelActive)触发，使用类型为LoginRequestMessage的消息发送登录请求
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 启动新的线程(若不单开一个就会使用NioEventLoopGroup中的线程，会阻塞其他操作)，接收用户输入的用户名和密码
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名：");
                                String username = scanner.nextLine();
                                if(EXIT.get()){
                                    return;
                                }
                                System.out.println("请输入密码：");
                                String password = scanner.nextLine();
                                if(EXIT.get()){
                                    return;
                                }
                                // 构造LoginRequestMessage消息对象，发送给服务器(应该做判空之类的校验)
                                LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                                // 写入内容后就会触发出站操作，就会往上执行各Handler(编码为符合协议的ByteBuf、打印日志。预设长度则是入站的不执行)
                                ctx.writeAndFlush(loginRequestMessage);

                                System.out.println("等待后续操作...");
                                try {
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // 走到这里说明服务端登录响应回来了
                                if (!IS_LOGIN.get()){
                                    // 失败就关闭，就会触发channel.closeFuture().sync();向下运行，而且最后走到finally中优雅关闭
                                    ctx.channel().close();
                                    return;
                                }
                                // 登录成功，就连接上服务器了，一直发送聊天消息即可
                                while (true){
                                    System.out.println("========================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [num1,num2,num3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("========================================");
                                    String command = null;
                                    try {
                                        command = scanner.nextLine();
                                    } catch (Exception e) {
                                        break;
                                    }
                                    if(EXIT.get()){
                                        return;
                                    }
                                    String[] split = command.split(" ");
                                    // 根据不同的指令类型构建不同的消息，下面假设格式都正确
                                    switch (split[0]){
                                        case "send":
                                            // 参数1：发送人即当前用户名  参数2：发送给哪个用户，即输入的用户名  参数3：发送内容，即输入的内容
                                            ctx.writeAndFlush(new ChatRequestMessage(username,split[1],split[2]));
                                            break;
                                        case "gsend":
                                            // 参数1：发送人，即当前用户名  参数2：发送给哪个聊天室，即输入的聊天室名  参数3：发送内容，即输入的内容
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username,split[1],split[2]));
                                            break;
                                        case "gcreate":
                                            // 参数1：创建的聊天室名称，即输入的聊天室名称  参数2：聊天室初始成员，即成员的set集合
                                            Set<String> set = new HashSet<>(Arrays.asList(split[2].split(",")));
                                            set.add(username);
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(split[1],set));
                                            break;
                                        case "gmembers":
                                            // 参数1：发起查询的人，即当前用户名  参数2：要获取成员的聊天室名称，即输入的聊天室名称
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(username,split[1]));
                                            break;
                                        case "gjoin":
                                            // 参数1：请求加入的用户，即当前用户名  参数2：要加入的聊天室名称，即输入的聊天室名称
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username,split[1]));
                                            break;
                                        case "gquit":
                                            // 参数1：请求退出的用户，即当前用户名  参数2：要退出的聊天室名称，即输入的聊天室名称
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username,split[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            },"System in").start();
                        }
                        // 用Channel的read()事件接收登录成功与否的服务端响应(这里的msg就是服务器返回的LoginResponseMessage)
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg:{}",msg);
                            // 如果是登录响应，就将登录成功与否的结果更新到客户端的登录状态中
                            if (msg instanceof LoginResponseMessage){
                                LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                if (loginResponseMessage.isSuccess()) {
                                    IS_LOGIN.set(true);
                                }
                                // 同时如果是登录响应，就执行countDown()，以便上面的System in线程执行登录响应后的操作
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        // 在连接断开时触发
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经断开，按任意键退出..");
                            EXIT.set(true);
                        }

                        // 在出现异常时触发
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("连接已经断开，按任意键退出..{}", cause.getMessage());
                            EXIT.set(true);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e){
            log.error("Client error",e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
