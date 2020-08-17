package com.gsz.community;

import com.gsz.community.dao.DiscussPostMapper;
import com.gsz.community.dao.LoginTicketMapper;
import com.gsz.community.dao.MessageMapper;
import com.gsz.community.dao.UserMapper;
import com.gsz.community.entity.DiscussPost;
import com.gsz.community.entity.LoginTicket;
import com.gsz.community.entity.Message;
import com.gsz.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10,0);
        for(DiscussPost post: list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);

        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testMessageMapper(){
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for(Message m: list){
            System.out.println(m);
        }

        System.out.println( messageMapper.selectConversationCount(111));

        List<Message> list2 = messageMapper.selectLetters("111_112",0,10);
        for(Message m: list2){
            System.out.println(m);
        }

        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectUnreadLetterCount(131,"111_131"));
    }

}
