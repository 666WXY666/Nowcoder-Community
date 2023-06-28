package com.nowcoder.community.util;

public class RedisKeyUtil {

    // Redis的key一般都是以冒号分隔的，这样可以分层，方便管理
    private static final String SPLIT = ":";

    // 实体的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个用户的前缀
    private static final String PREFIX_USER_LIKE = "like:user";

    // 某个用户关注的实体的前缀
    private static final String PREFIX_FOLLOWEE = "followee";

    // 某个实体拥有的粉丝的前缀
    private static final String PREFIX_FOLLOWER = "follower";

    // 登录验证码的前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 登录凭证的前缀
    private static final String PREFIX_TICKET = "ticket";

    // 用户的前缀
    private static final String PREFIX_USER = "user";

    // 获取某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 获取某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 获取某个用户的关注的实体的key
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return "followee" + SPLIT + userId + SPLIT + entityType;
    }

    // 获取某个实体拥有的粉丝的key
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return "follower" + SPLIT + entityType + SPLIT + entityId;
    }

    // 获取登录验证码的key
    // kaptcha:owner -> String(kaptcha的text)
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 获取登录凭证的key
    // ticket:ticket -> String(ticket对象的json字符串)
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 获取用户的key
    // user:userId -> String(user对象的json字符串)
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

}
