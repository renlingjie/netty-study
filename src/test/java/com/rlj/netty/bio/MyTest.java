package com.rlj.netty.bio;

import java.io.*;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-30
 */
public class MyTest {
    public static void main(String[] args) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream("mytest.txt"),4 * 1024);
        // 拿到的是内容的字节对象的ASCII(一个英文字符对应1个，一个汉文对应3个)，因此定义一个字节数组来存储
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        System.out.println(new String(bytes)); // 字节数组直接转String得到：hello world 哈哈哈
    }
}
