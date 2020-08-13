package com.gsz.community.dao;

import com.gsz.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    // 列表，而且支持分页，所以要提供俩方法，一个查询数据，一个查询行数
    // 然后查询未读消息数量

    //查询当前用户的会话列表
    //针对每个会话，只显示一条最新的数据
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectUnreadLetterCount(int userId, String conversationId);

    //新增加一条私信
    int insertMessage(Message message);

    //更改私信的状态
    int updateStatus(List<Integer> ids, int status);

    //显示通知
    //查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    //查询某个主题下通知的数量
    int selectNoticeCount(int userId, String topic);

    //查询某个主题下未读的数量
    int selectUnreadNoticeCount(int userId, String topic);

    //查询某个主题下的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
