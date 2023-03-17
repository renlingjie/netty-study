package com.rlj.server.handler;

import com.rlj.message.GroupCreateRequestMessage;
import com.rlj.message.GroupCreateResponseMessage;
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
// SimpleChannelInboundHandler可以指定一个关心的消息类型，这里指定创建聊天室请求的消息
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        String groupName = groupCreateRequestMessage.getGroupName();
        Set<String> members = groupCreateRequestMessage.getMembers();
        // 创建聊天室，先根据请求的聊天室名称查询聊天室是否存在，不存在才能创建
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null){
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,groupName + "创建成功"));
            // 创建成功后，给初始成员发送"您已加入XXX"的消息
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName));
            }
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false,groupName + "已经存在"));
        }
    }
}
