package com.rlj.netty.s4;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-15
 */
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、Netty中的Future也是关联线程池一起使用，但这里的线程池就是Netty的EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        // 2、使用线程池中的一个线程(EventLoop)执行下面任务，让主线程获取到这个任务返回的Future结果。
        // 也是有Callable、Runnable两类，因为Future需要接收返回结果，故用前者
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        // 3、主线程通过拿到的Future对象来获取结果，调用所说的get()方法，阻塞等待返回结果
        // log.debug("等待结果");
        // log.debug("结果是{}",future.get());

        // 4、但是Netty的Future更强大一些，除了使用同步的get()，也能使用异步的addListener()，这样主线程就
        // 不会像在get()处阻塞住，而是继续向下运行，当上面线程执行完Future中有值了，就会另起一个线程接收结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接收结果");
                log.debug("结果是{}",future.get());
            }
        });
    }
}
