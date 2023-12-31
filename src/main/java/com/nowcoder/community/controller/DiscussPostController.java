package com.nowcoder.community.controller;

//import com.nowcoder.community.annotation.LoginRequired;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 发布帖子-POST
    // @LoginRequired
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {

        // 从hostHolder中获取当前用户，判断是否登录（这里用不到了，因为有@LoginRequired）
        if (hostHolder.getUser() == null) {
            return CommunityUtil.getJsonString(403, "你还没有登录哦！");
        }

        // 将帖子信息存入数据库
        DiscussPost post = new DiscussPost();
        post.setUserId(hostHolder.getUser().getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        // 通过事件的发布者，将事件发布到kafka中
        // 事件的消费者：Elasticsearch服务，将帖子存入Elasticsearch中
        eventProducer.fireEvent(new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId())
                .setData("title", title)
                .setData("content", content));

        // 将帖子放入Redis缓存，等待定时任务计算帖子的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况，将来统一处理
        return CommunityUtil.getJsonString(0, "发布成功！");
    }

    // 帖子详情-GET
    // 这里的id是从路径中获取的
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 通过id获取帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 通过id获取帖子的作者
        // 也可以通过关联查询实现，效率较高，但是有耦合性
        model.addAttribute("user", userService.findUserById(post.getUserId()));
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        // 判断用户是否登录，如果登录，查询当前用户对该帖子的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        // 帖子的评论分页显示
        // 设置分页信息
        page.setLimit(5);
        // 设置路径
        page.setPath("/discuss/detail/" + discussPostId);
        // 设置总的评论数
        page.setRows(post.getCommentCount());
        // 获取帖子的评论列表
        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 通过帖子的id获取评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论显示列表，每个评论的信息和作者信息，commentViewObjectList
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        // 遍历评论列表，将每个评论的信息和作者信息存入map中，再将map存入list中
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 一个评论的显示信息
                Map<String, Object> commentVo = new HashMap<>();
                // 评论信息
                commentVo.put("comment", comment);
                // 评论作者信息
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                // 判断用户是否登录，如果登录，查询当前用户对该评论的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复的显示列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                // 遍历回复列表，将每个回复的信息和作者信息存入map中，再将map存入list中
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 一个回复的显示信息
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复信息
                        replyVo.put("reply", reply);
                        // 回复作者信息
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        // 判断用户是否登录，如果登录，查询当前用户对该回复的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        // 将回复的显示信息存入回复的显示列表中
                        replyVoList.add(replyVo);
                    }
                }
                // 将回复的显示列表存入评论的显示信息中
                commentVo.put("replies", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                // 将评论的显示信息存入评论的显示列表中
                commentVoList.add(commentVo);
            }
        }
        // 将评论的显示列表存入model中
        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";
    }

    // 置顶/取消置顶-POST
    // 异步请求
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        // 获取帖子状态
        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 置顶/取消置顶帖子
        int type = post.getType() == 1 ? 0 : 1;
        discussPostService.updateType(id, type);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);

        // 因为帖子更新了，要触发发帖事件
        // 通过事件的发布者，将事件发布到kafka中
        // 事件的消费者：Elasticsearch服务，将帖子存入Elasticsearch中
        eventProducer.fireEvent(new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id));

        // 返回结果
        return CommunityUtil.getJsonString(0, null, map);
    }

    // 加精/取消加精-POST
    // 异步请求
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        // 获取帖子状态
        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 加精/取消加精帖子
        // 这里1表示加精，点击后恢复正常；2表示删除，0表示正常，点击后都加精
        int status = post.getStatus() == 1 ? 0 : 1;
        discussPostService.updateStatus(id, status);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        // 因为帖子更新了，要触发发帖事件
        // 通过事件的发布者，将事件发布到kafka中
        // 事件的消费者：Elasticsearch服务，将帖子存入Elasticsearch中
        eventProducer.fireEvent(new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id));

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        // 返回结果
        return CommunityUtil.getJsonString(0, null, map);
    }

    // 删除-POST
    // 异步请求
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        // 删除帖子
        discussPostService.updateStatus(id, 2);
        // 因为帖子更新了，要触发删帖事件
        // 通过事件的发布者，将事件发布到kafka中
        // 事件的消费者：Elasticsearch服务，将帖子从Elasticsearch中删除
        eventProducer.fireEvent(new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id));
        // 返回结果
        return CommunityUtil.getJsonString(0);
    }
}
