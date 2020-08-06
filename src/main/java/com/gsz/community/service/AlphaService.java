package com.gsz.community.service;

import com.gsz.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("构造函数");
    }

    @PostConstruct
    public void init(){
        System.out.println("在构造之后，可以执行服务初始化");
    }

    @PreDestroy
    public void destrop(){
        System.out.println("销毁alpha service对象之前输出");
    }

    public String find(){
        return alphaDao.select();
    }
}
