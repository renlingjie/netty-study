package com.rlj.server.handler;

import com.rlj.message.RpcRequestMessage;
import com.rlj.message.RpcResponseMessage;
import com.rlj.server.service.RpcTestService;
import com.rlj.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-26
 */
@Slf4j
@ChannelHandler.Sharable
// 服务端处理RPC请求的Handler：根据RPC请求消息请求的方法，反射调用，最后再将结果封装成RPC响应消息返回
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        // 参数分别是序列号、要调用方法所在类的全限定类名、要调用的方法名称、方法返回值类型、方法参数类型(因为可能有多个，
        // 所以来一个Class数组，我们就一个String参数，所以数组也只有这个String.class)、方法参数集
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId()); // 将请求的SequenceId拿到，作为响应的SequenceId，使得请求与响应的SequenceId对应上
        try {
            // 找到接口的实现类，调用实现类的方法，因为没有引入Spring，无法直接通过接口找到它的实现类，所以这里默认接口+impl就是
            // 实现类。然后反射得到实现类的对象，用接口接收
            RpcTestService service = (RpcTestService) Class.forName(message.getInterfaceName() + "Impl").getDeclaredConstructor().newInstance();
            // 通过反射拿到接口对象(实际是实现类对象)中RpcRequestMessage方法名指定的方法对象
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            // 反射调用方法，拿到返回结果
            Object invoke = method.invoke(service, message.getParameterValue());
            // 将结果封装为RPC响应消息返回
            response.setReturnValue(invoke);
        } catch (Exception e) {
//            e.printStackTrace();
//            String msg = e.getCause().getMessage();
//            response.setExceptionValue(new Exception("远程调用出错:" + msg));
            response.setExceptionValue(e);
        }
        ctx.writeAndFlush(response);
    }

}
