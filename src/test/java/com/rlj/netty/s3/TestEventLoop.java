package com.rlj.netty.s3;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-03
 */
@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        // 1、创建事件循环组(NioEventLoopGroup()中可以指定线程数，如果不传或者传0，则会根据一定的规则自动设定)
        EventLoopGroup group = new NioEventLoopGroup(2);
        // 2、获取下一个事件循环对象(比如上面设置为2，next调用就会先是第1个，再调用就是第2个，再来又是第1个这样循环)
        System.out.println(group.next());
        // 3、尝试让一个事件循环对象执行一个普通任务(execute和submit方法都可以)
        group.next().submit(() ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("ok");
        });
        // 4、尝试让一个事件循环对象执行一个定时任务
        group.next().scheduleAtFixedRate(() -> {
            log.debug("ok");
        }, 0,1, TimeUnit.SECONDS); // 从第0s开始，每隔1s执行一次
        log.debug("main");
    }
}
