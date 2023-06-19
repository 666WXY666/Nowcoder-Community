package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class DiscussPost {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type; // 0-普通; 1-置顶;
    private int status; // 0-正常; 1-精华; 2-拉黑;
    private Date createTime;
    private int commentCount;
    private double score; // 用于计算帖子的热度
    private String tags; // 用于存储帖子的标签，以空格隔开
}
