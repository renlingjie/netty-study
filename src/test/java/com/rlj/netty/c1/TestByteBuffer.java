package com.rlj.netty.c1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-28
 */
public class TestByteBuffer {
    public static void main(String[] args) {
        try {
            FileChannel channel = new FileInputStream("data.txt").getChannel();
            // 准备缓冲区(设置为10个字节)
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true){
                // 从Channel读取数据，向Buffer中写入
                int len = channel.read(buffer);
                if (len == -1) break; // 如果读完了，就退出
                // 从Buffer中取出内容并打印
                buffer.flip(); // 切换至读模式
                while (buffer.hasRemaining()){ // 如果Buffer中有数据
                    byte b = buffer.get();  // 一次读一个字节
                    System.out.print((char)b);
                }
                buffer.clear(); // 切换至写模式
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
