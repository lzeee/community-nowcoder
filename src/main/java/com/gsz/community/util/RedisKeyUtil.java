package com.gsz.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

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

}
