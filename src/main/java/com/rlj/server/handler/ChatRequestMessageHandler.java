package com.rlj.server.handler;

import com.rlj.message.ChatRequestMessage;
import com.rlj.message.ChatResponseMessage;
import com.rlj.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定聊天请求的消息
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage chatRequestMessage) throws Exception {
        // 消息要发给哪个用户，再根据这个用户找到他对应的Channel，再将消息内容写入该Channel
        String to = chatRequestMessage.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        // Channel不为空说明对方在线，就发送消息；Channel为空说明对方不在线，就发送消息给请求方
        if (channel != null){
            channel.writeAndFlush(new ChatResponseMessage(chatRequestMessage.getFrom(),chatRequestMessage.getContent()));
        } else {
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方用户不存在或不在线"));
        }
    }
}
