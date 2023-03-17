package com.rlj.netty.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-31
 */
public class TestFilesCopyWalkFileTree {
    public static void main(String[] args) throws Exception{
        String source = "/Users/renlingjie/Downloads/软注代码";
        String target = "/Users/renlingjie/Downloads/软注代码copy";
        // 第一个参数是起点，第二个是遍历到文件后要做的操作
        Files.walkFileTree(Paths.get(source),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 创建的路径实际上就是将得到的当前路径的"软注代码"替换为"软注代码copy"
                String targetPath = dir.toString().replace(source,target);
                Files.createDirectory(Paths.get(targetPath));
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 拷贝的路径实际上就是将得到的当前路径的"软注代码"替换为"软注代码copy"
                String targetPath = file.toString().replace(source,target);
                Files.copy(file,Paths.get(targetPath));
                return super.visitFile(file, attrs);
            }
        });
    }
}
