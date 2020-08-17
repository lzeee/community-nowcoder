package com.gsz.community.quartz;

import com.gsz.community.entity.DiscussPost;
import com.gsz.community.service.DiscussPostService;
import com.gsz.community.service.ElasticSearchService;
import com.gsz.community.service.LikeService;
import com.gsz.community.util.CommunityConstant;
import com.gsz.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static Date epo;

    static {
        try {
            epo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            logger.info("无需刷新");
            return;
        }else{
            logger.info("刷新帖子分数:" + operations.size());
            while(operations.size()>0){
                this.refresh((Integer)operations.pop());
            }
            logger.info("刷新完毕");
        }
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post==null){
            logger.error("帖子不存在" + postId);
            return;
        }else {
            boolean wonderful = post.getStatus() == 1;
            int commentCount = post.getCommentCount();
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

            double w = (wonderful ? 75:0) + commentCount*10 + likeCount*2;
            double score = Math.log10(Math.max(w,1)) + (post.getCreateTime().getTime()-epo.getTime())/(1000*3600*24.0);
            //分数存在哪呢
            //更新数据库
            discussPostService.updateScore(postId,score);
            //更新搜索引擎中的数据
            post.setScore(score);
            elasticSearchService.saveDiscussPost(post);
        }
    }

}
