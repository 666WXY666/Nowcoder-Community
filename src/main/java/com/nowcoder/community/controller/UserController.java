package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 个人设置页面-GET
    // 添加注解，表示该方法需要登录才能访问
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    // 上传头像-POST
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        if (fileName == null || StringUtils.isBlank(fileName)) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            model.addAttribute("error", "文件格式不正确!（只支持*.png/*.jpg/*.jepg）");
            return "/site/setting";
        }

        String suffix = fileName.substring(index);
        if (StringUtils.isBlank(suffix) || !".png".equals(suffix) && !".jpg".equals(suffix) && !".jpeg".equals(suffix)) {
            model.addAttribute("error", "文件格式不正确!（只支持*.png/*.jpg/*.jepg）");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (Exception e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败, 服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        // 注册成功，返回重定向页面到首页
        model.addAttribute("msg", "修改头像成功，正在返回主页！");
        model.addAttribute("target", "/index");
        return "/site/operate-result";
        //        return "redirect:/index";
    }

    // 获取头像-GET
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀，注意这里需要+1，在下面拼接类型时，应该为image/jpg，而不是image/.jpg
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (// 读取文件
             FileInputStream fis = new FileInputStream(fileName);
             // 响应输出流
             OutputStream os = response.getOutputStream();) {
            // 缓冲区
            byte[] buffer = new byte[1024];
            // 记录每次读取的数据长度
            int b = 0;
            // 循环读取
            while ((b = fis.read(buffer)) != -1) {
                // 将缓冲区的数据输出到浏览器
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    // 修改密码-POST
    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            // 修改密码成功，返回重定向页面到登出页面
            // 注意这里不能直接重定向登录页面重新登陆，需要先登出，通过登出重定向到登录页面，否则会出现重复登陆的情况
            model.addAttribute("msg", "修改密码成功，正在前往登录页面，请重新登录!");
            model.addAttribute("target", "/logout");
            return "/site/operate-result";
            //            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }
}
