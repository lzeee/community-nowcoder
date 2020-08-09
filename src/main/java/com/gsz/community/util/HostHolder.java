package com.gsz.community.util;

import com.gsz.community.entity.User;
import org.springframework.stereotype.Component;

// 这里起到容器的作用，用来持有用户的信息
// 代替session对象，为什么这里不用session对象呢？
// 这里是针对线程的
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
