package com.gsz.community.controller.interceptor;

import com.gsz.community.entity.LoginTicket;
import com.gsz.community.entity.User;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CookieUtil;
import com.gsz.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        //有ticket才处理，没ticket说明没登录
        if(ticket != null){
            //查询登录凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                // 凭证有效，查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求过程中，要持有该user数据
                // 要考虑到多线程之间要进行隔离，每个请求都是用一个线程来处理的，使用ThreadLocal
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //在模板引擎调用之前，需要将user传入model中
        //方便在模板中使用
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //模板引擎执行完之后，把信息清理掉
        hostHolder.clear();
    }

}
