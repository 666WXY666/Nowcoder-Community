package com.nowcoder.community.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 1. 定义切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {
    }

    // 2. 定义通知
    // 前置通知
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 通过RequestContextHolder获取request对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost(); // 获取用户的ip地址
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // 获取当前时间
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(); // 获取类名方法名
        // 日志格式：用户[1.2.3.4]，在[xxx]，访问了[com.nowcoder.community.service.xxx()]
        logger.info(String.format("用户[%s]，在[%s]，访问了[%s]", ip, now, target));
    }
}
