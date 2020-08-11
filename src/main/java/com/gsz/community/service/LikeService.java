package com.gsz.community.service;

import com.gsz.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞/取消赞
    public void like(int userId, int entityType, int entityId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询在放在事务之外，否则不会执行
                boolean isMember =  redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if(isMember){
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else{
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                redisOperations.exec();
                return null;
            }
        });

    }

    // 查询某实体的点赞数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        // 找到key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 第一次为点赞，第二次为取消赞
        boolean isMember =  redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember){
            return 1; //点了赞
        }
        else{
            return 0; //没点赞
        }

    }

    // 查询某个用户被赞的总数
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }

}
