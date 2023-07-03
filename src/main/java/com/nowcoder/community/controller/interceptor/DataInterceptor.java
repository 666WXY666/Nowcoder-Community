package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    // 在Controller之前执行，统计UV和DAU
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // 统计DAU
        // 从hostHolder中获取当前登录用户
        User user = hostHolder.getUser();
        // 如果用户不为空，说明当前有用户登录，才统计DAU
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        // 返回true，表示继续执行后续的拦截器和Controller
        return true;
    }
}
