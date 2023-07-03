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

    // UV的前缀
    private static final String PREFIX_UV = "uv";

    // 活跃用户DAU的前缀
    private static final String PREFIX_DAU = "dau";

    // 帖子分数的前缀
    private static final String PREFIX_POST = "post";

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
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 获取某个实体拥有的粉丝的key
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
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

    // 获取单日UV的key
    // uv:日期 -> long
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 获取区间UV的key
    // uv:开始日期:结束日期 -> long
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 获取单日活跃用户DAU的key
    // dau:日期 -> long
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 获取区间活跃用户DAU的key
    // dau:开始日期:结束日期 -> long
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 获取需要更新帖子分数的帖子key
    // post:score -> set(postId)
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
