package com.rlj.server.handler;

import com.rlj.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Renlingjie
 * @name
 * @date 2023-03-01
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    // 客户端某个线程A(这里我们在main中进行，所以线程A实际上是主线程)进行方法调用，RpcResponseMessageHandler对应的线程B接收响应结果。
    // 然后要把结果给线程A，涉及到两个线程间的通信，使用Promise，又因为某次某方法的调用与返回结果一一对应。所以来一个Map，线程A进行调用
    // 并等待结果时，就以它调用时用的请求消息的SequenceId作为key，然后线程B接收到响应时，将响应消息的SequenceId获取，将结果Promise放到
    // 该SequenceId对应的Value中
    public static final Map<Integer, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponseMessage message) throws Exception {
        log.debug("{}",message);
        // 将结果放到Map集合中SequenceId对应的Promise中，并且将这个K-V移除(因为只需要赋值一下，让调用方法的线程结束等待，使用Promise即可)
        Promise<Object> promise = PROMISE_MAP.remove(message.getSequenceId());
        if (promise != null){
            Object returnValue = message.getReturnValue();
            Exception exceptionValue = message.getExceptionValue();
            if (exceptionValue != null){
                promise.setFailure(exceptionValue);
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
