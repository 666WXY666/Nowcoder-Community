package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class Event {

    private String topic; // 主题
    private int userId; // 触发事件的用户
    private int entityType; // 触发事件的实体类型
    private int entityId; // 触发事件的实体 id
    private int entityUserId; // 实体的作者
    private Map<String, Object> data = new HashMap<>(); // 其他数据

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
