package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表-GET
    @LoginRequired
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        int userId = hostHolder.getUser().getId();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(userId));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                // 当前会话总消息数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // 当前会话未读消息数量
                map.put("unreadCount", messageService.findLetterUnreadCount(userId, message.getConversationId()));
                int targetId = userId == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询当前用户总未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 查询当前用户总未读通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(userId, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    // 私信详情-GET
    @LoginRequired
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, int currentPage, Model mode) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                // 根据是不是我发送的消息，展示对话在左边还是右边
                if (message.getFromId() == hostHolder.getUser().getId()) {
                    map.put("isMe", 1);
                } else {
                    map.put("isMe", 0);
                }
                // 总是展示发送者
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        mode.addAttribute("letters", letters);

        // 私信目标
        mode.addAttribute("target", getLetterTarget(conversationId));

        // 私信所在的页数，用于返回当前页
        mode.addAttribute("currentPage", currentPage);

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    // 通过conversationId获取TargetId
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    // 获取私信列表中所有未读的对方私信的id
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                // 未读消息且是对方发的
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    // 发送私信-POST
    // 异步请求
    @LoginRequired
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 根据用户名查询用户
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJsonString(1, "目标用户不存在！");
        }

        // 构造消息
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        // 保证conversationId的大小顺序
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        // 发送消息
        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0);
    }

    // 删除私信-POST
    // 异步请求
    @LoginRequired
    @RequestMapping(path = "/letter/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJsonString(0);
    }

    // 通知列表-GET
    @LoginRequired
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        int userId = hostHolder.getUser().getId();
        // 查询评论类通知
        Message message = messageService.findLatestNotice(userId, TOPIC_COMMENT);
        model.addAttribute("commentNotice", handleMessageVO(message, TOPIC_COMMENT, userId));

        // 查询点赞类通知
        message = messageService.findLatestNotice(userId, TOPIC_LIKE);
        model.addAttribute("likeNotice", handleMessageVO(message, TOPIC_LIKE, userId));

        // 查询关注类通知
        message = messageService.findLatestNotice(userId, TOPIC_FOLLOW);
        model.addAttribute("followNotice", handleMessageVO(message, TOPIC_FOLLOW, userId));

        // 查询当前用户总未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 查询当前用户总未读通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(userId, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    // 根据message处理生成MessageVO
    private Map<String, Object> handleMessageVO(Message message, String topic, int userId) {
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            // 将content转换为map
            // 由于content是转义过的，需要解码
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 将json字符串转换为map
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            // 关注类通知不需要postId
            if (!Objects.equals(topic, TOPIC_FOLLOW)) {
                messageVO.put("postId", data.get("postId"));
            }

            // 查询topic类通知数量
            int count = messageService.findNoticeCount(userId, topic);
            messageVO.put("count", count);

            // 查询topic类通知未读数量
            int unread = messageService.findNoticeUnreadCount(userId, topic);
            messageVO.put("unread", unread);
        }
        return messageVO;
    }

    // 通知详情-GET
    @LoginRequired
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        int userId = hostHolder.getUser().getId();
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(userId, topic));

        // 查询通知列表
        List<Message> noticeList = messageService.findNotices(userId, topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                // 关注类通知不需要postId
                if (!Objects.equals(topic, TOPIC_FOLLOW)) {
                    map.put("postId", data.get("postId"));
                }
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVOList.add(map);
            }
        }
        model.addAttribute("notices", noticeVOList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

    // 删除通知-POST
    // 异步请求
    @LoginRequired
    @RequestMapping(path = "/notice/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteNotice(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJsonString(0);
    }
}
