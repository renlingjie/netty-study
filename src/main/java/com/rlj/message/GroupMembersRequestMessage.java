package com.rlj.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class GroupMembersRequestMessage extends Message {
    private String from;
    private String groupName;

    public GroupMembersRequestMessage(String from,String groupName) {
        this.from = from;
        this.groupName = groupName;
    }

    @Override
    public int getMessageType() {
        return GroupMembersRequestMessage;
    }
}
