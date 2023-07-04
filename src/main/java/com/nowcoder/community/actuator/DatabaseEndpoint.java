package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
// 通过访问 http://localhost:8080/actuator/database 可以看到数据库连接信息
@Endpoint(id = "database")
public class DatabaseEndpoint {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    // @ReadOperation代表GET请求访问该端点时，会调用该方法
    @ReadOperation
    public String checkConnection() {
        try (
                var ignored = dataSource.getConnection();
        ) {
            return CommunityUtil.getJsonString(0, "获取连接成功!");
        } catch (Exception e) {
            logger.error("获取连接失败: " + e.getMessage());
            return CommunityUtil.getJsonString(1, "获取连接失败!");
        }
    }
}
