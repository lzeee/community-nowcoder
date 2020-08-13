package com.gsz.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.gsz.community.dao.MessageMapper;
import com.gsz.community.entity.Message;
import com.gsz.community.entity.Page;
import com.gsz.community.entity.User;
import com.gsz.community.service.MessageService;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CommunityConstant;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

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
        //总的未读私信数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(),null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //总的未读通知数量
        int noticUnreadCount = messageService.findUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticUnreadCount",noticUnreadCount);

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

    //获取通知列表
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //三种通知
        //评论
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count", count);
            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread", unread);
        }
        else
        {
            messageVo.put("message", null);
        }

        model.addAttribute("commentNotice", messageVo);
        //点赞
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
        messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count", count);
            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread", unread);
        }
        else
        {
            messageVo.put("message", null);
        }
        model.addAttribute("likeNotice", messageVo);
        //关注
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count", count);
            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread", unread);
        }
        else
        {
            messageVo.put("message", null);
        }
        model.addAttribute("followNotice", messageVo);
        //页面内需要显示
        //总的未读数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticUnreadCount = messageService.findUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticUnreadCount",noticUnreadCount);
        return "/site/notice";
    }

    @RequestMapping(path="/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic, Page page, Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null){
            for(Message notice:noticeList){
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer)data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                //通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getUnreadLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }
}
