package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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


    // 发布帖子-POST
    @LoginRequired
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {

        // 从hostHolder中获取当前用户，判断是否登录
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
        return "/site/discuss-detail";
    }
}
