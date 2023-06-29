package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Getter
@Setter
@ToString
@Document(indexName = "discusspost")
public class DiscussPost {

    @Id
    private int id;// 帖子id

    @Field(type = FieldType.Integer)
    private int userId;// 发帖人的id

    // analyzer是存储时的分词器，应该尽可能详细，拆分尽可能多的词汇
    // searchAnalyzer是搜索时的分词器，应该尽可能模糊，拆分合适的词汇
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;// 帖子标题

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;// 帖子内容

    @Field(type = FieldType.Integer)
    private int type; // 0-普通; 1-置顶;

    @Field(type = FieldType.Integer)
    private int status; // 0-正常; 1-精华; 2-拉黑;

    @Field(type = FieldType.Date)
    private Date createTime;// 发布时间

    @Field(type = FieldType.Integer)
    private int commentCount;// 评论数量

    @Field(type = FieldType.Double)
    private double score; // 用于计算帖子的热度
}
