package com.rlj.netty.s6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-16
 */
public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        ByteBuf buf1 = buf.slice(0, 5); // 从位置1开始切，长度为5
        ByteBuf buf2 = buf.slice(5, 5); // 从位置5开始切，长度为5
        System.out.println(buf1.toString(Charset.defaultCharset()));
        System.out.println(buf2.toString(Charset.defaultCharset()));
        ByteBuf bufAll = ByteBufAllocator.DEFAULT.buffer(10);
        bufAll.writeBytes(buf1).writeBytes(buf2);
    }
}
