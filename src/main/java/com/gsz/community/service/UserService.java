package com.gsz.community.service;

import com.gsz.community.dao.LoginTicketMapper;
import com.gsz.community.dao.UserMapper;
import com.gsz.community.entity.LoginTicket;
import com.gsz.community.entity.User;
import com.gsz.community.util.CommunityConstant;
import com.gsz.community.util.CommunityUtil;
import com.gsz.community.util.MailClient;
import com.gsz.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    //@Autowired
    //private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int userId){
        //return userMapper.selectById(userId);
        User user = getCache(userId);
        if(user==null){
            user = initCache(userId);
        }
        return user;
    }

    //用户缓存
    //查询时，优先从缓存查找
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(redisKey);
    }
    //没找到，初始化缓存
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user,3600, TimeUnit.SECONDS);
        return user;
    }
    //变更时，清除原始缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
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

    // 用户激活
    public int activation(int userId, String code){
        //查用户，判断激活码对不对
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code))
        {
            userMapper.updateStatus(userId, 1);
            //清理缓存
            clearCache(userId);
            return ACTIVATION_SUCCES;
        }
        else{
            return ACTIVATION_FAILURE;
        }

    }

    // 用户登录
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        //空值判断
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "账号不存在!");
            return map;
        }
        //验证激活状态
        if(user.getStatus() == 0){
            map.put("usernameMsg", "账号未激活!");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        //这里少了一步，如果已经有ticket呢？？
        //没有任何问题，登录成功
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0); //有效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*expiredSeconds));

        //记录凭证
        //loginTicketMapper.insertLoginTicket(loginTicket);

        //用redis记录
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket); //对象会自动序列化成字符串

        //返回凭证
        //这里直接返回ticket就可以
        //后面根据ticket就可以判断
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //用户退出
    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket, 1);
        //这里是使redis中的ticket无效
        //取出对象修改
        //再覆盖对象
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    //凭证查询
    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }

    //更新头像
    public int updateHeader(int userId, String headerUrl){
        //return
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public int updatePassword(int userId, String password ){
        clearCache(userId);
        return userMapper.updatePassword(userId, password);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

}
