package com.rlj.netty.c1;

import java.nio.ByteBuffer;

import static com.rlj.netty.c1.ByteBufferUtil.debugAll;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-29
 */
public class TestByteBufferReadWrite {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61); // 'a'
        debugAll(buffer);
        buffer.put(new byte[]{0x62,0x63,0x64}); // 'b' 'c' 'd'
        debugAll(buffer);
        System.out.println(buffer.get()); // 没有切换至读模式就直接读
        buffer.put((byte) 0x65); // 读完后又写'e'，但Position此时已经后移一位，多了个为空的0('.')
        buffer.flip();
        System.out.println(buffer.get()); // 切换为写模式再读，就会读取第一个'a'
        debugAll(buffer);
        buffer.compact(); // 压缩后'a'就没有了。但是前移后原来的内容并不清空，而是等待写入的时候覆盖(Position作为写入位置就在它们前面)，比如下面的ee
        debugAll(buffer);
        buffer.put(new byte[]{0x66,0x67}); // 'f' 'g'
        debugAll(buffer);
    }
}
