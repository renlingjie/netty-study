package com.rlj.netty.s4;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
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
// 说明：无论是JDK的Future还是Netty的Future，它们都是被动的接收一个线程执行任务的返回结果，
// 而Promise
    @Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、Netty中的Promise也是关联线程池一起使用，这里的线程池就是Netty的EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        // 2、可以主动创建，而不是被动的拿到一个线程执行完返回的Future。不过都是存放结果的rong qi
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        // 3、某个线程(EventLoop)执行计算，计算完毕后向Promise填充结果
        eventLoop.submit(() -> {
            log.debug("执行计算");
            try {
                Thread.sleep(1000);
                // 执行到这里就是执行成功，就用setSuccess()填充结果，假设结果是50
                promise.setSuccess(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 执行到这里就是执行失败，就用setFailure()填充结果，这里填入失败原因
                promise.setFailure(e);
            }
        });
        // 4、主线程通过拿到的Promise对象来获取结果，调用所说的get()方法，阻塞等待返回结果
         log.debug("等待结果");
         log.debug("结果是{}",promise.get());

        // 5、Promise同样的，除了使用同步的get()，也能使用异步的addListener()，这样主线程就不会像
        // 在get()处阻塞住，而是继续向下运行，当上面线程执行完Future中有值了，就会另起一个线程接收结果
        // promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
        //   @Override
        //    public void operationComplete(Future<? super Integer> future) throws Exception {
        //        log.debug("接收结果");
        //        log.debug("结果是{}",future.get());
        //    }
        // });
    }
}
