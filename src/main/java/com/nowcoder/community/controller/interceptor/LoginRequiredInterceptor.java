package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    // 在Controller之前拦截判断是否登录
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断是否登录
        if (handler instanceof HandlerMethod handlerMethod) {
            // 获取方法对象
            Method method = handlerMethod.getMethod();
            // 获取方法上的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 判断是否有注解，有注解则需要登录
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 未登录，重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
