package com.gsz.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 生成随机的字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // 不够安全，所以一般会加盐
    // password + salt -> MD5加密，记录
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        else
        {
            return DigestUtils.md5DigestAsHex(key.getBytes());
        }
    }

}
