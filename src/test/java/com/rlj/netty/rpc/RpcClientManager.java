package com.rlj.netty.rpc;

import com.rlj.message.RpcRequestMessage;
import com.rlj.protocol.MessageCodec;
import com.rlj.protocol.ProtocolFrameDecoder;
import com.rlj.server.handler.RpcResponseMessageHandler;
import com.rlj.server.service.RpcTestService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-28
 */
// 改造的最终目的是谁想发消息，就拿到这个channel对象去发送即可
@Slf4j
public class RpcClientManager {

    public static void main(String[] args) {
        //    getChannel().writeAndFlush(new RpcRequestMessage(
        //            1,"com.rlj.server.service.RpcTestService","sayHello",
        //            String.class,new Class[]{String.class},new Object[]{"张三"}
        //    ));
        // 上面的使用平替为下面的
        RpcTestService proxyService = getProxyService(RpcTestService.class);
        System.out.println(proxyService.sayHello("张三"));
        // System.out.println(proxyService.sayHello("李四"));
    }

    // 优化3：创建代理类
    // RPC框架的实际使用者们是不会像下面这样用的，他们一般都是直接拿到远程对应方法的Service实现类对象，然后调用对象的对应方法的
    // 所以采用中介者模式，将下面的"原始手工的方法调用"通过代理类对象与我们的目标"Service实现类对象调用"建立起对应关系，后续
    // 我们只要使用"Service实现类对象调用"，通过代理类就会转换为下面的"原始手工的方法调用"
    //    getChannel().writeAndFlush(new RpcRequestMessage(
    //            1,"com.rlj.server.service.RpcTestService","sayHello",
    //            String.class,new Class[]{String.class},new Object[]{"张三"}
    //    ));
    public static <T> T getProxyService(Class<T> serviceClass){
        // 使用JDK自带的代理即可，三个参数分别是：类加载器、代理要实现的接口数组、代理类中方法执行的行为
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        Object result = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 之后代理类中的任何方法都会进入到这里去执行。三个参数分别为为：代理对象、代理类正在执行的方法、方法参数数组。这里只需要
            // 将上面的代码功能拆开，做两件事就能平替上面的功能：
            // 1、将方法调用转为RpcRequestMessage消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,                  // SequenceId用SequenceIdGenerator递增生成
                    serviceClass.getName(),      // 全限定接口类名，可以通过传入的Class调用getName()拿到
                    method.getName(),            // 方法名，可以通过传入的method调用getName()拿到
                    method.getReturnType(),      // 方法返回值类型，可以通过传入的method调用getReturnType()拿到
                    method.getParameterTypes(),  // 方法参数类型，可以通过传入的method调用getParameterTypes()拿到
                    args                         // 方法参数值，就是这里传入的args
            );
            // 2、将RpcRequestMessage消息对象发送出去
            getChannel().writeAndFlush(msg);

            // 3、发送请求消息后，就要等待结果，就要把接收结果的Promise准备好
            // DefaultPromise<>()需要传入EventExecutor，而getChannel().eventLoop()拿到的EventLoop就继承了EventExecutor
            // 其实就是用来指定Promise对象异步接收结果的线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISE_MAP.put(sequenceId,promise);

            // 4、然后就等着Promise有内容了，才会继续往下
            promise.await(); // 也可以sync()，await()是无论成功失败都不会抛异常，而sync()会

            // 5、走到这里就代表方法的调用结果接收到了
            if (promise.isSuccess()){
                return promise.getNow(); // 调用正常
            } else {
                throw new RuntimeException(promise.cause()); // 调用失败
            }
        });
        return (T)result;
    }


    private static Channel channel = null;
    private static final Object LOCK = new Object(); // 锁对象

    // 优化2：基于initChannel()拿到Channel的基础上，因为后面无论谁使用都使用这一个Channel即可，因此只用初始化一次，所以单例优化
    public static Channel getChannel() {
        if (channel != null){
            return channel;
        }
        // 双重检测锁，这是因为可能有两个或多个线程同时走到这里，如果没有下面的判断，当第一个线程执行initChannel()，拿到Channel
        // 返回后，第二个线程拿到锁进来还是会执行initChannel()，因此加一层判断，这样第二个线程进来就不会再次执行initChannel()
        synchronized (LOCK){
            if (channel != null){
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 优化1：无论何时何地使用，都能够拿到Channel
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        // RPC响应消息处理器
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

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
        try {
            // 建立起连接后，就将得到的channel赋值给上面的Channel
            channel = bootstrap.connect("localhost", 8077).sync().channel();
            // 如果还使用同步阻塞关闭，则别的线程调用该方法时永远拿不到channel，因为代码执行到这里阻塞住了，故采用异步方式关闭
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("Client error",e);
        }
    }
}
