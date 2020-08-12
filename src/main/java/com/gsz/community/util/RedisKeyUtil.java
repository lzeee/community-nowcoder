package com.gsz.community.util;

import javax.imageio.plugins.tiff.TIFFDirectory;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    //验证码前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";

    //登录凭证
    private static final String PREFIX_TICKET = "ticket";

    //用户缓存
    private static final String PREFIX_USER = "user";


    // 生成某个实体的赞的key
    // like:entity:entityType:entityId --> set(userId) 赞相当于set的size
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 生成某个用户总的被赞量
    // like:user:userId --> int
    public static  String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 两种：
    // 有关注者列表，有粉丝列表

    //某个用户关注的实体
    //followee:userId:entityType --> zset(entityId, time)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的关注者
    //follower:entityType:entityId --> zset(userId, time)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //验证码的key
    public static String getKaptchaKey(String owner){
        //验证码如何唯一标识呢？
        //用一个凭证
        return PREFIX_KAPTCHA + SPLIT + owner;

    }

    //登录凭证的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户的key
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
}
