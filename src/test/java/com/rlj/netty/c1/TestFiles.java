package com.rlj.netty.c1;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Renlingjie
 * @name
 * @date 2022-12-31
 */
public class TestFiles {
    public static void main(String[] args) throws Exception{
        Files.createDirectory(Paths.get("XXX"));
    }
}
