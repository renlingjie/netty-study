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
public class TestFilesWalkFileTree {
    public static void main(String[] args) throws Exception{
        // 没法用int，因为匿名类只能访问该匿名类类外面类的用final修饰的变量，但是用final修饰，则没办法进行++操作。为什么要final修饰？
        // 底层原理是因为匿名类和该类外面的类的局部变量不在同一个工作空间，所以不能使用基本类型，因为基本类型是保存在栈里的，地址可变。除非
        // 用final修饰。所以这里就得用地址不可变的引用类型。这里使用倒不是因为它线程安全，而是因为他是一个引用类型。但是也不能用Integer
        // 类型，因为它看似是一个引用类型，但是当值变了的话，地址也会变化
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        // 第一个参数是起点，第二个是遍历到文件后要做的操作
        Files.walkFileTree(Paths.get("/Users/renlingjie/Downloads/文件压缩"),new SimpleFileVisitor<Path>(){
            /**
             * SimpleFileVisitor有四个方法可以重写，选择需要重写的进行重写
             * 1、preVisitDirectory：遍历到文件夹之前的操作
             * 2、visitFile：遍历到文件的操作
             * 3、visitFileFailed：遍历文件失败的操作
             * 4、postVisitDirectory：遍历文件夹失败的操作
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("遍历到的文件夹为：" + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("遍历到的文件为：" + file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("遍历到的文件夹数量：" + dirCount);
        System.out.println("遍历到的文件数量：" + fileCount);
    }
}
