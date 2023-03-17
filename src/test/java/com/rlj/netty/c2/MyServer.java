package com.rlj.netty.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import static com.rlj.netty.c1.ByteBufferUtil.debugAll;

/**
 * @author Renlingjie
 * @name
 * @date 2023-01-14
 */
@Slf4j
public class MyServer {
    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从source读，向target写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target); // 老师工具类的一个方法，能打印ByteBuffer的内容
            }
        }
        source.compact();
    }

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 处理之前或处理完毕，必须将事件对应的SelectionKey移除。实在不需要“处理+移除”模式的，可以用cancel()将事件取消
                iter.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel c = (ServerSocketChannel) key.channel();
                    SocketChannel sc = c.accept();
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, SelectionKey.OP_READ);
                    // 1、向客户端发送内容，且只进行一次写入。如果数据量超过ByteBuffer大小，会走到下面if分支，添加一个写事件，当客户端读取完所有
                    // ByteBuffer中的数据后才会触发，这个时候服务端才能立刻就写入
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    int write = sc.write(buffer);
                    System.out.println("该次实际写入字节:" + write);
                    // 2、如果有剩余未读字节，才需要关注写事件
                    if (buffer.hasRemaining()) {
                        // 在原有关注事件的基础上，增加关注写事件。为什么使用加法就可以？因为这里事件的value采用了位运算，accept是1，read是2，write是4
                        // 1+4=5只可能是accept和write
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        // 把ByteBuffer作为附件和SocketChannel对应的SelectionKey关联(因为下次触发后，这次的ByteBuffer中继续写，里面可能还有客户端
                        // 还没读完的数据)
                        sckey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    // 3、走到这里，说明客户端读完ByteBuffer中的数据，触发可写事件，让Selector明白可以继续让服务端写入数据了
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    System.out.println("该次实际写入字节:" + write);
                    // 如果全部写完了，就把Selector监听的可写事件移除，并移除之前关联的ByteBuffer(释放内存)
                    if (!buffer.hasRemaining()) {
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                        key.attach(null);
                    }
                }
            }
        }
    }
}