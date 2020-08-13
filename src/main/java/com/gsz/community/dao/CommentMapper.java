package com.gsz.community.dao;

import com.gsz.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //分页查询
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    //一共有多少条数据
    int selectCountByEntity(int entityType, int entityId);

    //插入一条评论
    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
