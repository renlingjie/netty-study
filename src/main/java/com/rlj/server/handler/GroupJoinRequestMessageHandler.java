package com.rlj.server.handler;

import com.rlj.message.GroupCreateResponseMessage;
import com.rlj.message.GroupJoinRequestMessage;
import com.rlj.message.GroupJoinResponseMessage;
import com.rlj.server.session.Group;
import com.rlj.server.session.GroupSession;
import com.rlj.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-19
 */
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定加入聊天室请求的消息
@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage groupJoinRequestMessage) throws Exception {
        String groupName = groupJoinRequestMessage.getGroupName();
        String username = groupJoinRequestMessage.getUsername();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        // 成功时返回聊天室对象, 如果聊天室不存在返回null
        Group group = groupSession.joinMember(groupName, username);
        if (group != null){
            ctx.writeAndFlush(new GroupJoinResponseMessage(true,"加入聊天室[" + groupName + "]成功"));
            // 创建成功后，给所有成员发送"欢迎XXX加入XXX"的消息
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "欢迎" + username + "加入" + groupName));
            }
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false,"聊天室[" + groupName + "]不存在"));
        }
    }
}
