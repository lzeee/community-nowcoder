package com.gsz.community.controller;

import com.gsz.community.dao.MessageMapper;
import com.gsz.community.entity.Message;
import com.gsz.community.entity.Page;
import com.gsz.community.entity.User;
import com.gsz.community.service.MessageService;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CommunityUtil;
import com.gsz.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    //处理消息列表
    @RequestMapping(path="/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        //还要显示
        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList!=null){
            for(Message m:conversationList){
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", m);
                //当前会话未读几条
                map.put("unreadcount", messageService.findUnreadLetterCount(user.getId(),m.getConversationId()));
                //当前会话一共几条
                map.put("lettercount", messageService.findLetterCount(m.getConversationId()));
                int targetId = user.getId() == m.getFromId()? m.getToId():m.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        //总的未读消息数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(),null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }


    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        //分页
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //查询所有私信
        //数据不够，还要显示发信人的头像、名字，也即from_id
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message m: letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter", m);
                map.put("fromUser", userService.findUserById(m.getFromId()));
                letters.add(map);
            }
        }
        //把信都存进来
        model.addAttribute("letters", letters);
        // 明确目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //把未读消息改为已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        //设置已读
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getUnreadLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList!=null){
            for(Message letter: letterList){
                //当未读并且发信人为对方的时候，才会改为已读
                if(hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0){
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId){
        String[] ids= conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }


    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

}
