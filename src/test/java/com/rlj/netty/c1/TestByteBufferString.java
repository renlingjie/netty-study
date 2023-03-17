package com.rlj.netty.c1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.rlj.netty.c1.ByteBufferUtil.debugAll;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-29
 */
public class TestByteBufferString {
    public static void main(String[] args) {
        // 1、字符串转ByteBuffer
        // 方法1:转字节数组后写入。说明：执行完后还是写模式(Position位于内容末尾)
        ByteBuffer buffer1 = ByteBuffer.allocate(10);
        buffer1.put("hello".getBytes());
        // 方法2：Charset的encode。说明：执行完后自动切换为读模式(Position位于内容开头)
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        // 方法3：用wrap()将字节数组包装成ByteBuffer。说明：执行完后自动切换为读模式(Position位于内容开头)
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());

        // 2、ByteBuffer转字符串
        // 说明：若使用上面的buffer1，还需要flip()切换为读模式将Position指向0位置，这里就使用buffer2或buffer3做演示
        // 方法1：Charset的decode。
        String str1 = StandardCharsets.UTF_8.decode(buffer2).toString();
        // 方法2：也可以拿到ByteBuffer的字节数组后转字符串，不赘述
        int limit = buffer1.limit();
    }
}
