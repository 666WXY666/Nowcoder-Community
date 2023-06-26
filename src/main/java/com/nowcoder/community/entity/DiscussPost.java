package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class DiscussPost {
    private int id;// 帖子id
    private int userId;// 发帖人的id
    private String title;// 帖子标题
    private String content;// 帖子内容
    private int type; // 0-普通; 1-置顶;
    private int status; // 0-正常; 1-精华; 2-拉黑;
    private Date createTime;// 发布时间
    private int commentCount;// 评论数量
    private double score; // 用于计算帖子的热度
    private String tags; // 用于存储帖子的标签，以空格隔开
}
