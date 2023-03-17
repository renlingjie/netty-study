package com.rlj.server.handler;

import com.rlj.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// 处理客户端正常、异常退出
// 因为不关系消息，只关心两个事件：异常事件、ChannelInactive事件，所以继承最原始的ChannelInboundHandlerAdapter
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {
    // 客户端连接正常断开时会触发channelInactive事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开了自然要把Session中维护的这个断开的Channel移除掉
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{}已经正常断开",ctx.channel());
    }

    // 客户端连接异常断开时会触发exceptionCaught事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 断开了自然要把Session中维护的这个断开的Channel移除掉
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{}已经异常断开，异常是{}",ctx.channel(),cause.getMessage());
    }
}
