package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Comment {
    private int id;// 评论id
    private int userId;// 评论用户id
    private int entityType;// 评论实体类型，1-帖子、2-评论
    private int entityId;// 评论实体id
    private int targetId;// 评论目标id，如果没有则为0，如果有则为用户id
    private String content;// 评论内容
    private int status;// 评论状态，0-正常、1-删除、2-置顶
    private Date createTime;// 评论创建时间
}
