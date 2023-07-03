package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 用mapper.xml的方式写SQL语句
    // orderMode: 0-默认排序（按照时间排序），1-按照热度排序
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // @Param注解用于给参数取别名，如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    // 增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    // 根据id查询帖子
    DiscussPost selectDiscussPostById(int id);

    // 更新帖子评论数量
    int updateCommentCount(int id, int commentCount);

    // 更新帖子类型
    int updateType(int id, int type);

    // 更新帖子状态
    int updateStatus(int id, int status);

    // 更新帖子分数
    int updateScore(int id, double score);

}
