package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;

// @ControllerAdvice，用于修饰类，表示该类是Controller的全局配置类
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // @ExceptionHandler，用于修饰方法，在Controller出现异常时执行，用于全局异常处理
    // @ModelAttribute，用于修饰方法，在Controller方法执行之前，SpringMVC会先逐个调用@ModelAttribute修饰的方法，用于为Model对象添加属性，绑定参数
    // @InitBinder，用于修饰方法，在Controller方法执行之前执行，该方法不能有返回值，它必须声明为void，用于绑定参数的转换器
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        // 遍历异常栈
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        // 判断请求类型是同步请求还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        // 异步请求，返回JSON字符串
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 设置响应内容类型
            response.setContentType("application/plain;charset=utf-8");
            // 获取响应输出流
            PrintWriter writer = response.getWriter();
            // 将异常信息写入响应输出流
            writer.write(CommunityUtil.getJsonString(1, "服务器异常！"));
        } else {
            // 同步请求，重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
