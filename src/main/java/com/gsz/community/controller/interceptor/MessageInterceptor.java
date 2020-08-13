package com.gsz.community.controller.interceptor;

import com.gsz.community.entity.User;
import com.gsz.community.service.MessageService;
import com.gsz.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //在controller处理完成后
        User user = hostHolder.getUser();
        if(user != null && modelAndView !=null){
            int letterUnread = messageService.findUnreadLetterCount(user.getId(),null);
            int noticeUnread = messageService.findUnreadNoticeCount(user.getId(),null);
            modelAndView.addObject("unreadCount", letterUnread+noticeUnread);
        }
    }
}
