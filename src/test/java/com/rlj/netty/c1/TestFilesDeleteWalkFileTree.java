package com.rlj.netty.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-31
 */
public class TestFilesDeleteWalkFileTree {
    public static void main(String[] args) throws Exception{

        // 第一个参数是起点，第二个是遍历到文件后要做的操作
        Files.walkFileTree(Paths.get("/Users/renlingjie/Downloads/onlinemap1"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
