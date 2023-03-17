package com.rlj.netty.s4;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-15
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、JDK中的Future一般是关联线程池一起使用，所以我们创建一个固定大小的线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 2、使用线程池中的一个线程执行下面任务，让主线程获取到这个任务返回的Future结果。而任务类型
        // 有Callable、Runnable两类，前者有返回结果后者没有。因为Future需要接收返回结果，故用前者
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        // 3、主线程通过拿到的Future对象来获取结果，调用所说的get()方法，阻塞等待返回结果
        log.debug("等待结果");
        log.debug("结果是{}",future.get());
    }
}
