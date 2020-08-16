package com.gsz.community.util;

public interface CommunityConstant {
    int ACTIVATION_SUCCES = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    int REMEMBER_EXPIRED_SECONDS = 3600*24*100;

    //实体类型 帖子
    int ENTITY_TYPE_POST = 1;
    //评论
    int ENTITY_TYPE_COMMENT = 2;
    //用户
    int ENTITY_TYPE_USER = 3;

    //kafka主题
    //评论
    String TOPIC_COMMENT="comment";
    //点赞
    String TOPIC_LIKE = "like";
    //关注
    String TOPIC_FOLLOW = "follow";
    //发帖
    String TOPIC_PUBLISH = "publish";
    //删帖
    String TOPIC_DELETE = "delete";


    //系统用户id
    int SYSTEM_USERID = 1;

    //用户权限
    String AUTHORITY_USER = "user";
    String AUTHORITY_ADMIN = "admin";
    String AUTHORITY_MODERATOR = "moderator";
}
