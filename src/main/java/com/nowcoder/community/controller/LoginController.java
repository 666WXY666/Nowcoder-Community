package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
// import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 注入SecurityContextLogoutHandler这个Bean，用于退出登录
    @Autowired
    private SecurityContextLogoutHandler securityContextLogoutHandler;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 注册-GET
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // 登录-GET
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // 注册-POST
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //注册成功，返回重定向页面到首页
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            //注册失败，返回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            // 这里不用再addAttribute user了，因为user已经参数中自动装载到model中了
            return "/site/register";
        }
    }

    // 激活-GET
    // http://localhost:8080/community/activation/{user_id}/{activation_code}
    @RequestMapping(path = "activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            // 激活成功，返回重定向页面到登录页面
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            // 重复激活，返回重定向页面到首页
            model.addAttribute("msg", "无效操作，该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            // 激活失败，返回重定向页面到首页
            model.addAttribute("msg", "激活失败，您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 生成验证码-GET
    // Session重构为Redis
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session（重构）
        // session.setAttribute("kaptcha", text);

        // 验证码的归属（临时凭证，用于区分用户，这里不能用userId，因为用户还没登陆）
        String kaptchaOwner = CommunityUtil.generateUUID();
        // 将临时归属凭证存入Cookie
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        // 设置Cookie的生存时间
        cookie.setMaxAge(60);
        // 设置Cookie的生效范围
        cookie.setPath(contextPath);
        // 将Cookie发送给客户端
        response.addCookie(cookie);

        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    // 登录-POST
    // Session重构为Redis
    // 注意这里的method = RequestMethod.POST，因为这里是提交表单，不是get请求
    // 只要method不重复，路径path可以重复
    // 注意这里如果是对象，会自动装载到model中
    // 如果是基本类型，要么手动加入到model中，要么写一个request对象，request.getParameter(username)获取，
    // 要么在动态htmlTemplate里使用thymeleaf的${param.username}获取
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码（重构）
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        // 从Cookie中获取验证码的归属
        // 判断Cookie是否失效
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            // 从Redis中获取验证码
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !code.equalsIgnoreCase(kaptcha)) {
            // 验证码错误，返回登录页面
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        // 检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            // 登录成功，将ticket存入cookie
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 设置cookie的有效路径
            cookie.setPath(contextPath);
            // 设置cookie的有效时间
            cookie.setMaxAge(expiredSeconds);
            // 将cookie添加到response中
            response.addCookie(cookie);
            // 返回重定向页面到首页
            // 注意这里是重定向，而不是直接返回/index页面，因为这里是POST，不是GET请求
            return "redirect:/index";
        } else {
            // 登录失败，返回登录页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            // 这里不用再addAttribute user了，因为user已经参数中自动装载到model中了
            return "/site/login";
        }
    }

    // 退出登录-GET
    // @LoginRequired
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket, HttpServletRequest request,
                         HttpServletResponse response, Authentication authentication) {
        userService.logout(ticket);
        // 重构，使用Spring Security
        // 使用SecurityContextLogoutHandler清理SecurityContext
        securityContextLogoutHandler.logout(request, response, authentication);
        // 退出登录，返回重定向页面到登录页面
        // 因为login有两个请求，一个是GET请求，一个是POST请求
        // 这里要重定向默认到/login的GET请求
        return "redirect:/login";
    }

    // 忘记密码-GET
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    // 获取验证码-GET
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        Map<String, Object> map = userService.getForgetCode(email);
        if (map.containsKey("verifyCode")) {
            // 保存验证码，注意这里要对不同的邮箱保存不同的验证码，防止换邮箱后验证码还是之前的
            session.setAttribute(email + "_verifyCode", map.get("verifyCode"));
            return CommunityUtil.getJsonString(0);
        } else {
            return CommunityUtil.getJsonString(1, (String) map.get("emailMsg"));
        }
    }

    // 重置密码-POST
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        // 检查验证码
        String code = (String) session.getAttribute(email + "_verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            // 验证码错误，返回重置密码页面
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map == null || map.isEmpty()) {
            // 重置密码成功，跳转到登录页面
            model.addAttribute("msg", "重置密码成功，正在前往登录页面，请重新登录!");
            model.addAttribute("target", "/login");
            return "/site/operate-result";
            // return "redirect:/login";
        } else {
            // 重置密码失败，返回重置密码页面
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}
