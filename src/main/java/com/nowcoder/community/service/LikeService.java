package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 点赞
    // userId：谁点的赞
    // entityType：点赞的类型
    // entityId：点赞的实体id
    // entityUserId：被点赞的用户id
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 这里有两个操作，所以要用事务
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 某个用户对某个实体点赞的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 某个实体的赞的数量的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 判断用户是否已经点过赞
                // 注意：这里是查询，要放在事务之外
                boolean isMember = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(entityLikeKey, userId));
                // 开启事务
                operations.multi();
                if (isMember) {
                    // 如果已经点过赞，就取消点赞
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    // 如果没有点过赞，就点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                // 提交事务
                return operations.exec();
            }
        });


    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        // 某个用户对某个实体点赞的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 注意处理空值，也就是没有key的情况
        Long size = redisTemplate.opsForSet().size(entityLikeKey);
        return size == null ? 0 : size;
    }

    // 查询某人对某实体的点赞状态
    // 1：点赞
    // 0：未点赞
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // 某个用户对某个实体点赞的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 判断用户是否已经点过赞
        boolean isMember = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(entityLikeKey, userId));
        return isMember ? 1 : 0;
    }

    // 查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId) {
        // 某个实体的赞的数量的key
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        // 注意处理空值，也就是没有key的情况
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
