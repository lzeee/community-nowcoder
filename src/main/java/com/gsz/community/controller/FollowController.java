package com.gsz.community.controller;

import com.gsz.community.entity.Page;
import com.gsz.community.entity.User;
import com.gsz.community.service.FollowService;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CommunityConstant;
import com.gsz.community.util.CommunityUtil;
import com.gsz.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //关注是异步操作
    @RequestMapping(path = "/follow", method= RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        // 如果用户没登录呢，应该有一个检查
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType, entityId);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @RequestMapping(path = "/unfollow", method= RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        // 如果用户没登录呢，应该有一个检查
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType, entityId);

        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //分页
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        //查找所有关注的用户
        List<Map<String, Object>> userList = followService.findFollowee(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            for(Map<String, Object> map: userList){
                //要判断当前登录的用户对打开页面中用户的关注情况
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //分页
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        //查找所有的粉丝
        List<Map<String, Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            for(Map<String, Object> map: userList){
                //要判断当前登录的用户对打开页面中用户的关注情况
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }



    private boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        else{
            return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
    }

}