package com.gsz.community.controller;

import com.gsz.community.entity.DiscussPost;
import com.gsz.community.entity.Page;
import com.gsz.community.entity.User;
import com.gsz.community.service.DiscussPostService;
import com.gsz.community.service.LikeService;
import com.gsz.community.service.UserService;
import com.gsz.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //这里会自动的实例化model和page，并且会把page注入给model
    //所以在thymelead中可以直接访问page对象中的数据
    @RequestMapping(path="/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost post: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user=  userService.findUserById(post.getUserId());
                map.put("user", user);
                //赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @RequestMapping(path="/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


}
