package com.rlj.server.session;

import io.netty.channel.Channel;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
// 用户聊天会话
public interface Session {
    // 绑定会话。参数1指定哪个Channel要绑定会话，参数2指定会话绑定的用户
    void bind(Channel channel,String username);
    // 解绑会话。参数1指定哪个Channel要解绑会话
    void unbind(Channel channel);
    // 获取属性。参数1指定获取哪个Channel的属性，参数2指定要获取的属性名
    Object getAttribute(Channel channel,String name);
    // 设置属性。参数1指定设置哪个Channel的属性，参数2、3指定要设置的属性名与值
    void setAttribute(Channel channel,String name,Object value);
    // 查看该用户名对应的Channel是哪个。比如要给用户A发送消息，实际上要先根据其用户名找到Channel再发送
    Channel getChannel(String username);
}
