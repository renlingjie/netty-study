package com.rlj.netty.c1;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-31
 */
public class TestTransferTo {
    public static void main(String[] args) {
        try {
            FileChannel from = FileChannel.open(FileSystems.getDefault().getPath("data.txt"), StandardOpenOption.READ,StandardOpenOption.WRITE);
            FileChannel to = FileChannel.open(FileSystems.getDefault().getPath("qqq.txt"), StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);
            long size = from.size();
            // 文件剩余未传内容大小，最开始为from的size，每次循环减去当前transferTo传输的字节，刚好transferTo返回值就是传输了多少字节
            for (long left = size; left > 0;){
                // 每次传输时position的位置=总量-剩余量，比如总量5G，剩余3G，说明Position在5-3=2G
                // 传输的数量就是每次剩余量
                left -= from.transferTo(size - left,left,to);
            }
        } catch (Exception e){
        }
    }
}
