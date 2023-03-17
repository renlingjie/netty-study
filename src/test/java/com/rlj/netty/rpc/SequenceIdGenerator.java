package com.rlj.netty.rpc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Renlingjie
 * @name
 * @date 2023-03-16
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();
    public static int nextId(){
        return id.getAndIncrement();
    }
}
