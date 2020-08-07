package com.gsz.community.service;

import com.gsz.community.dao.UserMapper;
import com.gsz.community.entity.User;
import com.gsz.community.util.CommunityConstant;
import com.gsz.community.util.CommunityUtil;
import com.gsz.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }


    // 注册的业务逻辑
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //都不为空之后
        //验证是否已经存在
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg", "该账号已被注册");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }
        //开始注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));//加盐
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //加入数据库，其中id是自增长
        userMapper.insertUser(user);
        //利用模板渲染
        //发激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "gsz论坛激活", content);
        return map;
    }

    // 激活
    public int activation(int userId, String code){
        //查用户，判断激活码对不对
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode() == code)
        {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCES;
        }
        else{
            return ACTIVATION_FAILURE;
        }

    }

}
