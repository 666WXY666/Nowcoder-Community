package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine: 本地缓存
    // 核心接口：Cache，LoadingCache，AsyncLoadingCache
    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // 初始化缓存
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        // 这里可以使用二级缓存：Redis -> MySQL

                        logger.debug("从数据库中获取帖子列表!");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    logger.debug("从数据库中获取帖子数量!");
                    return discussPostMapper.selectDiscussPostRows(key);
                });
    }

    // 通过用户id查询帖子
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 判断是否需要启用缓存
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("从数据库中获取帖子列表!");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    // 通过用户id查询帖子数量
    public int findDiscussPostRows(int userId) {
        // 判断是否需要启用缓存
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("从数据库中获取帖子数量!");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 添加帖子
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    // 通过帖子id查询帖子
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 更新帖子评论数量
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    // 更新帖子类型
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    // 更新帖子状态
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    // 更新帖子分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
