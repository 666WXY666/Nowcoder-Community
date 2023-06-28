package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    // 重构
    // @Autowired
    // private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        // 重构
        // return userMapper.selectById(id);
        // 先从缓存中取值
        User user = getCache(id);
        if (user == null) {
            // 缓存中没有，从数据库中取值
            user = initCache(id);
        }
        return user;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    // 注册
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));//随机生成5位字符串
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));//加密
        user.setType(0);//普通用户
        user.setStatus(0);//未激活
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", (int) (Math.random() * 1000)));//随机头像
        user.setCreateTime(new Date());//当前时间
        // 插入数据库，一开始没有userId，但是插入数据库后就有了
        // 因为在配置文件中配置了useGeneratedKeys="true" keyProperty="id"
        userMapper.insertUser(user);
        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/{user_id}/{activation_code}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);//将模板和数据结合
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    // 激活账号
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 已经激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 激活码正确
            userMapper.updateStatus(userId, 1);// 更新状态
            // 数据变更，清除缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            // 激活码不正确
            return ACTIVATION_FAILURE;
        }
    }

    // 登录
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());// 随机生成凭证
        loginTicket.setStatus(0);// 0表示有效，1表示无效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));// 过期时间

        // 插入数据库（重构）
        // loginTicketMapper.insertLoginTicket(loginTicket);
        // 用redis存储凭证
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 存储凭证，用json格式字符串存储
        // Redis会自动将对象序列化为json格式字符串
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());// 将凭证返回给客户端

        return map;
    }

    // 退出
    public void logout(String ticket) {
        // 1表示无效
        // 重构
        // loginTicketMapper.updateStatus(ticket, 1);
        // 用redis删除凭证，不是真的删除，只是将status改为1，然后存储回去
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 取出凭证
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        // 设置为无效
        loginTicket.setStatus(1);
        // 存储回去
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    // 查询凭证
    public LoginTicket findLoginTicket(String ticket) {
        // 重构
        // return loginTicketMapper.selectByTicket(ticket);
        // 用redis查询凭证
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    // 更新头像
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 数据变更，清除缓存
        clearCache(userId);
        return rows;
    }

    // 获取验证码
    public Map<String, Object> getForgetCode(String email) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        // 检查邮箱是否存在
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱不存在!");
            return map;
        }
        // 检查是否已经激活
        if (user.getStatus() == 0) {
            map.put("emailMsg", "该邮箱未激活!");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);
        map.put("verifyCode", code);
        return map;
    }

    // 重置密码
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱不存在!");
            return map;
        }
        // 验证密码是否和原密码相同
        password = CommunityUtil.md5(password + user.getSalt());
        if (user.getPassword().equals(password)) {
            map.put("passwordMsg", "新密码不能和原密码相同!");
            return map;
        }
        // 更新密码
        userMapper.updatePassword(user.getId(), password);
        // 数据变更，清除缓存
        clearCache(user.getId());
        return map;
    }

    // 修改密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 验证密码是否和原密码相同
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if (user.getPassword().equals(newPassword)) {
            map.put("newPasswordMsg", "新密码不能和原密码相同!");
            return map;
        }

        // 更新密码
        userMapper.updatePassword(userId, newPassword);
        // 数据变更，清除缓存
        clearCache(userId);

        // 发送通知邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String content = templateEngine.process("/mail/update-password", context);
        mailClient.sendMail(user.getEmail(), "修改密码", content);
        return map;
    }

    // 1、从缓存中取用户
    // 这里Redis会自动将对象序列化为json格式字符串，在取出时会自动反序列化为对象
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2、取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 1小时过期
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3、数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
