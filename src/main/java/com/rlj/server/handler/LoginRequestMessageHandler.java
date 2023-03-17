package com.rlj.server.handler;

import com.rlj.message.LoginRequestMessage;
import com.rlj.message.LoginResponseMessage;
import com.rlj.server.service.UserServiceFactory;
import com.rlj.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定登录请求的消息
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage loginRequestMessage) throws Exception {
        String username = loginRequestMessage.getUsername();
        String password = loginRequestMessage.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage message;
        if (login) {
            // 连接成功后，将当前登录用户与当前Channel绑定，以后根据该用户名就能拿到对应的Channel，然后就可以发送消息了
            SessionFactory.getSession().bind(ctx.channel(), username);
            message = new LoginResponseMessage(true, "登录成功");
        } else {
            message = new LoginResponseMessage(false, "用户名或密码错误");
        }
        // 写入内容后就会触发出站操作，就会往上执行各Handler(编码为符合协议的ByteBuf、打印日志。预设长度则是入站的不执行)
        ctx.writeAndFlush(message);
    }
}
