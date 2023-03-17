package com.rlj.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
// 用户聊天室会话
public interface GroupSession {
    // 创建聊天室。参数1指定聊天室名称，参数2指定聊天室初始成员
    // 成功时返回聊天室对象, 失败返回null
    Group createGroup(String name, Set<String> members);

    // 加入聊天室。参数1指定加入的聊天室名称，参数2指定加入聊天室的成员名
    // 成功时返回聊天室对象, 如果聊天室不存在返回null
    Group joinMember(String name, String member);

    // 移除成员。参数1指定要移除成员的聊天室名称，参数2要移除的成员名
    // 成功时返回聊天室对象, 如果聊天室不存在返回null
    Group removeMember(String name, String member);

    // 删除聊天室。参数1指定要删除的聊天室名称
    // 成功时返回聊天室对象, 如果聊天室不存在返回null
    Group removeGroup(String name);

    // 获取组成员。参数1指定要获取哪个聊天室的组成员
    // 返回组成员集合, 如果聊天室不存在或没有组成员会返回empty set
    Set<String> getMembers(String name);

    // 获取组成员的Channel集合。参数1指定要获取哪个聊天室的组成员的Channel集合，只有在线的Channel才会返回
    List<Channel> getMembersChannel(String name);
}
