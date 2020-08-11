package com.gsz.community.service;

import com.gsz.community.dao.MessageMapper;
import com.gsz.community.entity.Message;
import com.gsz.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations( int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit){
        return  messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId){
        return  messageMapper.selectLetterCount(conversationId);
    }

    public int findUnreadLetterCount(int userId, String conversationId){
        return messageMapper.selectUnreadLetterCount(userId, conversationId);
    }

    //添加私信
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    //将私信设为已读
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids, 1);
    }

}

