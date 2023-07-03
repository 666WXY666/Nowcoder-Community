package com.nowcoder.community.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class WkConfig {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init() {
        //创建wk图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            if (!file.mkdir()) {
                logger.error("创建WK图片目录失败: " + wkImageStorage);
            }
            logger.info("创建WK图片目录: " + wkImageStorage);
        }
    }

}
