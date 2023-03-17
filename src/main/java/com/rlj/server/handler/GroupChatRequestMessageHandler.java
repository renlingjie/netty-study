package com.rlj.server.handler;

import com.rlj.message.GroupChatRequestMessage;
import com.rlj.message.GroupChatResponseMessage;
import com.rlj.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定聊天室聊天请求的消息
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        // TODO 1、群聊发送消息不要给自己发 2、判断当前给群聊发送消息的用户是否在这个群聊中(但实际上不用，前端保证即可)
        List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(groupChatRequestMessage.getGroupName());
        channels.forEach(channel -> {
            channel.writeAndFlush(new GroupChatResponseMessage(groupChatRequestMessage.getFrom(), groupChatRequestMessage.getContent()));
        });
    }
}
