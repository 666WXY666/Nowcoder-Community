package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig implements CommunityConstant {

    // 忽略静态资源的访问
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/resources/**");
    }

    // 授权
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 授权请求
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                ).hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                ).requestMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_MODERATOR
                ).requestMatchers(
                        "/discuss/delete",
                        "/data/**"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                ).anyRequest().permitAll()
        );
        // 权限不够时的处理
        http.exceptionHandling(handle -> handle.authenticationEntryPoint(
                // 没有登录时的处理
                (request, response, authException) -> {
                    // 判断是否是异步请求
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        response.getWriter().write(CommunityUtil.getJsonString(403, "你还没有登录哦!"));
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                }
        ).accessDeniedHandler(
                (request, response, accessDeniedException) -> {
                    // 没有权限时的处理
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        response.getWriter().write(CommunityUtil.getJsonString(403, "你没有访问此功能的权限!"));
                    } else {
                        response.sendRedirect(request.getContextPath() + "/denied");
                    }
                }
        ));
        // Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout(logout -> logout.logoutUrl("/securitylogout"));
        // 默认开启防止CSRF攻击
        return http.build();
    }

    // 在SecurityConfig中增加配置SecurityContextRepository
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // 配置一个LogoutHandler
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }
}
