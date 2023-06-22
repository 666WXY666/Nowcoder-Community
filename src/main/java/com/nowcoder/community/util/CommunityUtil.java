package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson2.JSONObject;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // md5加密，只能加密，不能解密，加随机字符串salt
    // hello -> abc123def456
    // hello + 3e4a8 -> abc123def456abc
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // 生成JSON字符串
    public static String getJsonString(int code, String msg, Map<String, Object> map) {

        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    // 生成JSON字符串（重载）
    public static String getJsonString(int code, String msg) {
        return getJsonString(code, msg, null);
    }

    // 生成JSON字符串（重载）
    public static String getJsonString(int code) {
        return getJsonString(code, null, null);
    }
}
