package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Message {
    private int id;
    private int fromId; // 发送者id，1表示系统通知
    private int toId; // 接收者id
    private String conversationId; // 会话id，冗余属性，fromId_toId或toId_fromId，小的在前
    private String content; // 消息内容
    private int status; // 消息状态，0-未读，1-已读，2-删除
    private Date createTime; // 创建时间
}
