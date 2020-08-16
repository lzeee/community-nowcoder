package com.gsz.community.controller;

import com.gsz.community.entity.DiscussPost;
import com.gsz.community.entity.Page;
import com.gsz.community.service.ElasticSearchService;
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
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> searchResult = elasticSearchService.searchDiscussPost(keyword,page.getCurrent()-1,page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(searchResult!=null){
            for(DiscussPost post:searchResult){
                Map<String, Object>map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //分页
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult == null ? 0:(int)searchResult.getTotalElements());

        //返回模板
        return "/site/search";
    }

}