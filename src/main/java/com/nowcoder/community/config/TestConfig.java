package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class TestConfig {

    @Bean
    // 这里返回值是Bean的类型，方法名是Bean的名字
    // 该方法的返回对象会被Spring容器自动装配容器中
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
