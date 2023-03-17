package com.rlj.server.session;

import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
// 聊天室(组)
public class Group {
    // 聊天室名称、成员
    private String name;
    private Set<String> members;

    public static final Group EMPTY_GROUP = new Group("empty", Collections.emptySet());

    public Group(String name, Set<String> members) {
        this.name = name;
        this.members = members;
    }
}
