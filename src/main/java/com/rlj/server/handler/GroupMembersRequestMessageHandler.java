package com.rlj.server.handler;

import com.rlj.message.GroupMembersRequestMessage;
import com.rlj.message.GroupMembersResponseMessage;
import com.rlj.server.session.GroupSessionFactory;
import com.rlj.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定查询聊天室成员请求的消息
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage groupMembersRequestMessage) throws Exception {
        String from = groupMembersRequestMessage.getFrom();
        String groupName = groupMembersRequestMessage.getGroupName();
        // 返回组成员集合, 如果聊天室不存在或没有组成员会返回empty set
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
        // 将得到的组成员集合返回给查询的用户
        Channel channel = SessionFactory.getSession().getChannel(from);
        channel.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
