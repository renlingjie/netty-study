package com.rlj.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rlj.netty.c1.ByteBufferUtil.debugAll;

/**
 * @author Renlingjie
 * @name
 * @date 2023-01-15
 */
// 启动后当前主线程作为Boss线程，该线程的Selector只负责监听ServerSocketChannel的accept事件，一旦有SocketChannel连接进来触发该
// accept事件，就会负载均衡到一个Worker线程中(首次会创建，之后则直接使用)，但是因为该Worker线程在执行run方法时在selector.select()
// 阻塞住，我们无法让该线程执行SocketChannel的register()方法来关注读事件，因此使用队列，将前面的任务先加到该线程的任务队列中，之后通过
// wakeup唤醒线程后，线程可以执行了，就从队列中拿到前面的任务，就可以关注读事件，一旦SocketChannel写入数据，在该线程的run()中就会像之前
// 那样不再阻塞在selector.select()处，该线程就会读取数据去了
@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector bossSelector = Selector.open();
        // 只让Boss线程的Selector(bossSelector)监听ServerSocketChannel的accept事件
        ssc.register(bossSelector, SelectionKey.OP_ACCEPT,null);
        // ServerSocketChannel监听8080端口，一旦有SocketChannel连接进来就会产生accept事件
        ssc.bind(new InetSocketAddress(8080));
        // 创建一些(为了释放性能，数量>=CPU核心数，下面为查询并设置为可用核心数)用于处理读事件的Worker线程，并初始化。
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        // 每次自增后取模，保证连接能负载均衡到workers的worker中
        AtomicInteger index = new AtomicInteger();
        while (true){
            // 一直监听，直至ServerSocketChannel有accept事件
            bossSelector.select();
            // 此时必然已经有accept事件了，就拿到已经触发了的accept事件
            Iterator<SelectionKey> iterator = bossSelector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()){
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                    SocketChannel sc = serverSocketChannel.accept();
                    sc.configureBlocking(false);
                    log.debug("有客户端连接过来了...{}",sc.getRemoteAddress());
                    // 将事件交给Worker的Selector，而不再是上面Boss的Selector
                    log.debug("准备将客户端连接的写事件交给Worker处理...{}",sc.getRemoteAddress());
                    workers[index.getAndIncrement() % workers.length].init(sc);
                    log.debug("已经将客户端连接的写事件交给Worker处理...{}",sc.getRemoteAddress());
                }
            }
        }
    }

    @Slf4j
    static class Worker implements Runnable{
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean flag = false;
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        public Worker(String name) {
            this.name = name;
        }
        // 初始化该Worker对应的线程和Selector。实际上只需要初次使用到该Worker的时候调用，但我们不知道是不是初次，所
        // 以每次都会调用，但应该只有第一次调用才生效(不能每次调用都生成一个新的Thread和Selector)，因此用个flag判断
        public void init(SocketChannel sc) throws IOException {
            // 当前worker就是一个Runnable接口实现类，之后线程执行的代码就在下面的run方法中。名字就是构造方法传入的
            if (!flag){
                selector = Selector.open();
                thread = new Thread(this,name);
                thread.start();
                flag = true;
            }
            queue.add(() -> {
                try {
                    sc.register(selector,SelectionKey.OP_READ,null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 因为上面thread.start()启动Worker线程的run方法，会在selector.select();阻塞住，而又要让Worker线程
            // 注册下面的read事件，就需要先唤醒一次Worker线程，然后注册完事件后，在下一次的while (true)循环中，因为
            // 已经有注册的事件了，Worker线程就会进行处理。还有一种情况是先唤醒了，然后主线程调用该init()，worker后阻
            // 塞，但是不影响，因为wakeup()像一个门票，只要有就消耗一张然后就不阻塞，所以即使是后阻塞，也因为有门票啦而
            // 放行，让下面的read事件成功注册上
            selector.wakeup();
        }

        @Override
        public void run() {
            // Worker的职责就是监听处理读写事件
            while (true){
                try {
                    selector.select();
                    Runnable task = queue.poll();
                    if (task != null){
                        task.run();
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        // 必然是SocketChannel的读写事件，这里暂时就不考虑读的连接断开和边界问题、写的数据量大问题
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel sc = (SocketChannel) key.channel();
                            log.debug("有客户端的SocketChannel中有数据，触发了Worker的读事件...{}",sc.getRemoteAddress());
                            int read = sc.read(buffer);
                            if (read == -1){
                                key.cancel();
                                sc.close();
                            } else {
                                buffer.flip();
                                debugAll(buffer);
                            }
                        }
                        iterator.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
